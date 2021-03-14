/*
 * Copyright 2021 Laszlo Attila Toth
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

package me.laszloattilatoth.jada.config;

import java.util.Map;

public class ProxyOptions {
    public void load(Map<String, Object> globalOptions, Map<String, Object> options) throws InvalidOptions {}

    public static class InvalidOptions extends Exception {
        public InvalidOptions(String s) {
            super(s);
        }
    }
}
