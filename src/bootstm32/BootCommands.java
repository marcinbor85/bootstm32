/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bootstm32;

import gnu.io.CommPort;
import gnu.io.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 *
 * @author marcin
 */
public class BootCommands {
    private ArrayList<BootCMD> listCmd;
    
    public BootCMD cmdGetCommands;
    public BootCMD cmdGetVersionReadStatus;
    public BootCMD cmdGetID;
    public BootCMD cmdReadMemory;
    public BootCMD cmdGo;
    public BootCMD cmdWriteMemory;
    public BootCMD cmdErase;
    public BootCMD cmdExtendedErase;
    public BootCMD cmdWriteProtect;
    public BootCMD cmdWriteUnprotect;
    public BootCMD cmdReadoutProtect;
    public BootCMD cmdReadoutUnprotect;
    
    private BootInterface bootInterface;

    public BootCommands(BootInterface _int) {
        bootInterface=_int;
        
        listCmd = new ArrayList<>();
        
        listCmd.add(cmdGetCommands = new BootCMD(0x00, "cmdGetCommands", bootInterface));
        listCmd.add(cmdGetVersionReadStatus = new BootCMD(0x01, "cmdGetVersionReadStatus", bootInterface));
        listCmd.add(cmdGetID = new BootCMD(0x02, "cmdGetID", bootInterface));
        listCmd.add(cmdReadMemory = new BootCMD(0x11, "cmdReadMemory", bootInterface));
        listCmd.add(cmdGo = new BootCMD(0x21, "cmdGo", bootInterface));
        listCmd.add(cmdWriteMemory = new BootCMD(0x31, "cmdWriteMemory", bootInterface));
        listCmd.add(cmdErase = new BootCMD(0x43, "cmdErase", bootInterface));
        listCmd.add(cmdExtendedErase = new BootCMD(0x44, "cmdExtendedErase", bootInterface));
        listCmd.add(cmdWriteProtect = new BootCMD(0x63, "cmdWriteProtect", bootInterface));
        listCmd.add(cmdWriteUnprotect = new BootCMD(0x73, "cmdWriteUnprotect", bootInterface));
        listCmd.add(cmdReadoutProtect = new BootCMD(0x82, "cmdReadoutProtect", bootInterface));
        listCmd.add(cmdReadoutUnprotect = new BootCMD(0x92, "cmdReadoutUnprotect", bootInterface));
    }
    
    public boolean setCmdAvailable(int _c) {
        for(BootCMD cmd : listCmd) {
            if (cmd.getCmd()==(byte)_c) {
                cmd.setAvailable(true);
                return true;
            }
        }
        return false;
    }    
    
    
    
    

}
