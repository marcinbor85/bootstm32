/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bootstm32;

import java.io.IOException;

/**
 *
 * @author marcin
 */
public interface BootInterface {
    public void sendByte(int _b) throws IOException;
    public void sendBuf(byte[] _b) throws IOException;
    public int recvByte() throws IOException;
    public boolean waitForACK() throws IOException;
    public boolean waitForACK(int _i, int _send) throws IOException;
    public void sendCmdRequest(BootCMD _cmd) throws IOException;
}
