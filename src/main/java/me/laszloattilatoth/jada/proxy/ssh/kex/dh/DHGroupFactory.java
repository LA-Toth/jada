/*
 * Copyright 2026 Laszlo Attila Toth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.laszloattilatoth.jada.proxy.ssh.kex.dh;

import java.security.SecureRandom;

public class DHGroupFactory {
    private static final SecureRandom secureRandom = new SecureRandom();

    private static BestModuliDesc getBestModuliDesc(int min, int wantBits, int max) {
        int bestCount = 0;
        int bestSize = 0;

        for (int i = 0; i < Moduli.modulii.size(); i++) {
            int size = Moduli.modulii.get(i).size();
            if (min <= size && size <= max) {
                if ((size > wantBits && size < bestSize) || (size > bestSize && bestSize < wantBits)) {
                    bestSize = size;
                    bestCount = 0;
                }

                if (size == bestSize) {
                    bestCount += 1;
                }
            }
        }
        return new BestModuliDesc(bestCount, bestSize);
    }

    private static Moduli getSelectedModuli(int bestSize, int selectedIndex) {
        Moduli selected = null;
        int selectionCount = 0;

        for (int i = 0; i < Moduli.modulii.size(); i++) {
            Moduli moduli = Moduli.modulii.get(i);
            if (moduli.size() == bestSize) {
                selectionCount += 1;
                if (selectionCount == selectedIndex) {
                    selected = moduli;
                }
            }
        }

        if (selected == null) {
            // can't happen
            selected = Moduli.modulii.get(1);
        }
        return selected;
    }

    public DHGroup createGroupForSize(int min, int wantBits, int max) {
        BestModuliDesc result = getBestModuliDesc(min, wantBits, max);

        final int selectedIndex = secureRandom.nextInt(result.bestCount());

        Moduli selected = getSelectedModuli(result.bestSize(), selectedIndex);
        return new DHGroup(selected.generator(), selected.modulus());
    }

    public DHGroup createGroup1() {
        return new DHGroup(Constants.gen, Constants.group1Mod);
    }

    public DHGroup createGroup14() {
        return new DHGroup(Constants.gen, Constants.group14Mod);
    }

    public DHGroup createGroup16() {
        return new DHGroup(Constants.gen, Constants.group16Mod);
    }

    public DHGroup createGroup18() {
        return new DHGroup(Constants.gen, Constants.group18Mod);
    }

    private record BestModuliDesc(int bestCount, int bestSize) {
    }
}
