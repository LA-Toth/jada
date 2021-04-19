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

package me.laszloattilatoth.jada.proxy.ssh.kex.dh;

import me.laszloattilatoth.jada.proxy.ssh.kex.digest.Digest;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;
import org.apache.sshd.common.util.buffer.BufferUtils;

import java.math.BigInteger;

public class DH {
    protected String V_C;
    protected String V_S;
    protected byte[] I_S;
    protected byte[] I_C;
    protected Digest digest;
    protected byte[] k;
    protected byte[] h;

    private byte[] e;
    private BigInteger eValue;
    private byte[] f;
    private BigInteger fValue;

    protected DH(Digest digest) {
        this.digest = digest;
    }

    public void init(String V_C, String V_S, byte[] I_C, byte[] I_S) {
        this.V_C = V_C;
        this.V_S = V_S;
        this.I_C = I_C;
        this.I_S = I_S;
    }

    public byte[] e() { return e;}

    public byte[] f() { return f;}

    public byte[] h() { return h;}

    public byte[] k() { return k;}

    public BigInteger eValue() {
        if (eValue == null) {
            eValue = BufferUtils.fromMPIntBytes(e);
        }
        return eValue;
    }

    public BigInteger fValue() {
        if (fValue == null) {
            fValue = BufferUtils.fromMPIntBytes(f);
        }
        return fValue;
    }

    public DH setE(byte[] e) {
        this.e = e;
        if (eValue != null)
            eValue = null;

        return this;
    }

    public DH setF(byte[] f) {
        this.f = f;
        if (fValue != null)
            fValue = null;

        return this;
    }

    public Packet prepareBuffer() {
        Packet b = new Packet();
        b.putString(V_C);
        b.putString(V_S);
        b.putRawBytes(I_C);
        b.putRawBytes(I_S);
        return b;
    }
}
