/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bootstm32;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author marcin
 */
public class BootCMD {
    private String name;
    private byte cmd;
    private boolean available;
    private BootInterface bootInt;
    
    public BootCMD(int _c, String _name, BootInterface _i) {
        cmd=(byte)_c;
        name=_name;
        available=false;
        bootInt=_i;
    }
    public String getName() {
        return name;
    }
    public byte getCmd() {
        return cmd;
    }
    public void setAvailable(boolean _b) {
        available=true;
    }
    public boolean isAvailable() {
        return available;
    }
    public boolean sendCmd() throws Exception {
        bootInt.sendCmdRequest(this);
        boolean stat=bootInt.waitForACK();
        return stat;
    }
}
