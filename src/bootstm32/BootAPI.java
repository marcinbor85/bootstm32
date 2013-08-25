/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bootstm32;

import gnu.io.CommPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author marcin
 */
public class BootAPI implements BootInterface {
            
    public static final byte ACK = 0x79;
    public static final byte NACK = 0x1F;
    
    public static final int ERASE_WAIT_DELAY = 20;
    public static final int CONNECT_WAIT_DELAY = 10;
    public static final int WRITE_WAIT_DELAY = 5;
    
    public static final int WRITE_PACKET_SIZE = 256;
    public static final int READ_PACKET_SIZE = 256;
    
    public static final int DEBUG_LEVEL = 0;
    
    public static final int DEBUG_MESSAGE = 10;
    public static final int DEBUG_ERROR = 10;
    public static final int DEBUG_SERIALPORT = 0;
    public static final int DEBUG_VISUAL = 10;
    public static final int DEBUG_API = 20;
            
    private static OutputStream outStream;
    private static InputStream inStream;

    private byte bootVersion;
    private byte[] productID;
    
    private BootCommands bootCmd;
    private ProductPids prodPids;
    
    private MyDebug debug;    
    
    public void setDebugLevel(int _d) {
        debug.setLevel(_d);
    }
    public BootAPI(OutputStream _out, InputStream _in) {
        outStream=_out;
        inStream=_in;

        bootVersion=0;
        productID = new byte[2];
        productID[0]=0;
        productID[1]=0;
        
        bootCmd = new BootCommands(this);
        prodPids = new ProductPids();
        
        debug = new MyDebug(System.out, System.err,this.getClass().getName(), DEBUG_LEVEL);
    }
    
    public boolean getID() throws Exception {
        int data;
        int i;
        int len;

        debug.log(DEBUG_VISUAL, "--------------------");
        debug.log(DEBUG_MESSAGE, "Checking device ID");
        
        if (bootCmd.cmdGetID.sendCmd()==false) return false;
        data=recvByte();
        if (data==-1) return false;
       
        len=data+1;
        
        if (len!=2) return false;
      
        for (i=0; i<len; i++) {
            data=recvByte();
            if (data==-1) return false;
            productID[i]=(byte)data;
        }
        int pid = productID[0]<<8|productID[1];
        
        if (waitForACK()==false) return false;

        debug.log(DEBUG_API, String.format("Product ID = 0x%04X",pid)+" -> "+prodPids.getProductName(pid));
        
        return true;
    }
    
    
    
    private boolean getVersionAndCommands() throws Exception {
        int data;
        int i;
        int len;
        
        debug.log(DEBUG_VISUAL, "--------------------");
        debug.log(DEBUG_MESSAGE, "Checking bootloader version");
        
        if (bootCmd.cmdGetCommands.sendCmd()==false) return false;
        
        data=recvByte();
        if (data==-1) return false;
        
        len=data;
        
        data=recvByte();
        if (data==-1) return false;
       
        bootVersion=(byte)(data&0xFF);
        
        for (i=0; i<len; i++) {
            data=recvByte();
            if (data==-1) return false;
            bootCmd.setCmdAvailable(data);
        }
        
        if (waitForACK()==false) return false;
        
        debug.log(DEBUG_API, String.format("Bootloader version = %1d.%1d",bootVersion/16,bootVersion%16));
        
        return true;
    }
    
    public boolean eraseAll() throws Exception {

        debug.log(DEBUG_VISUAL, "--------------------");
        debug.log(DEBUG_API, "Performing mass erase");
        
        if (bootCmd.cmdExtendedErase.isAvailable()) {
            if (bootCmd.cmdExtendedErase.sendCmd()==false) return false;
            sendBuf(new byte[] {(byte)0xFF, (byte)0xFF, (byte)0x00});
        } else if (bootCmd.cmdErase.isAvailable()) {
            if (bootCmd.cmdErase.sendCmd()==false) return false;
            sendBuf(new byte[] {(byte)0xFF, (byte)0x00});
        } else return false;
        
        debug.log(DEBUG_MESSAGE, "Erasing started");
        
        if (waitForACK(ERASE_WAIT_DELAY,-1)==false) return false;
        
        debug.log(DEBUG_MESSAGE, "Erasing successfull");
        
        return true;
    }
    
    public boolean write(int _adr, byte[] _data) throws Exception {
        int crc;
        int len;
        
        debug.log(DEBUG_VISUAL, "--------------------");
        debug.log(DEBUG_MESSAGE, String.format("Uploading data at 0x%08X ",_adr));
        
        if (bootCmd.cmdWriteMemory.sendCmd()==false) return false;
                
        debug.log(DEBUG_MESSAGE, "Uploading address");
        
        sendBuf(new byte[] {(byte)(_adr>>24), (byte)(_adr>>16), (byte)(_adr>>8), (byte)(_adr>>0), (byte)((_adr>>24)^(_adr>>16)^(_adr>>8)^(_adr>>0))});
       
        if (waitForACK()==false) return false;
       
        debug.log(DEBUG_MESSAGE, "Uploading data");
        len=_data.length-1;
        crc=0;
        sendByte(len);     
        crc^=len;
        sendBuf(_data);
        for (int i=0; i<_data.length; i++) crc^=_data[i];
        sendByte(crc);    
 
        if (waitForACK()==false) return false;
        
        debug.log(DEBUG_MESSAGE, "Uploading successfull");
        return true;
    }
    
    public boolean read(int _adr, byte[] _data, int _len) throws Exception {

        int data;
        int len;
        debug.log(DEBUG_VISUAL, "--------------------");
        debug.log(DEBUG_MESSAGE, String.format("Downloading data from 0x%08X ",_adr));
        
        if (bootCmd.cmdReadMemory.sendCmd()==false) return false;
        
        debug.log(DEBUG_MESSAGE, "Uploading address");

        sendBuf(new byte[] {(byte)(_adr>>24), (byte)(_adr>>16), (byte)(_adr>>8), (byte)(_adr>>0), (byte)((_adr>>24)^(_adr>>16)^(_adr>>8)^(_adr>>0))});
        
        if (waitForACK()==false) return false;
       
        len=_len-1;
        sendBuf(new byte[] {(byte)(len), (byte)(~len)});
       
        if (waitForACK()==false) return false;
       
        debug.log(DEBUG_MESSAGE, "Downloading data");
        
        for (int i=0; i<_len; i++) {
            data=recvByte();
            if (data==-1) return false;
            _data[i]=(byte)data;            
        }
        
        debug.log(DEBUG_MESSAGE, "Downloading successfull");
        return true;
    }
    
    public boolean readoutProtect() throws Exception {
        debug.log(DEBUG_VISUAL, "--------------------");
        debug.log(DEBUG_API, "Enabling readout protection");
        
        if (bootCmd.cmdReadoutProtect.sendCmd()==false) return false;
       
        if (waitForACK()==false) return false;
      
        debug.log(DEBUG_MESSAGE, "Readout protection enabled successfull");
        return true;
    }
    
    public boolean readoutUnprotect() throws Exception {
        debug.log(DEBUG_VISUAL, "--------------------");
        debug.log(DEBUG_API, "Disabling readout protection");
        
        if (bootCmd.cmdReadoutUnprotect.sendCmd()==false) return false;
       
        debug.log(DEBUG_MESSAGE, "Disabling started");
                
        if (waitForACK(ERASE_WAIT_DELAY,-1)==false) return false;
       
        debug.log(DEBUG_MESSAGE, "Readout protection disabled successfull");
        return true;
    }
    
    public boolean writeProtect(int _startSector, int _lenSector) throws Exception {
        debug.log(DEBUG_VISUAL, "--------------------");
        debug.log(DEBUG_API, "Enabling write protection");
        
        if (bootCmd.cmdWriteProtect.sendCmd()==false) return false;
           
        int len=_lenSector-1;
        int crc=0;
        sendByte(len);
        crc^=len;
        byte buf[] = new byte[_lenSector];
        for (int i=0; i<_lenSector; i++){
            buf[i]=(byte)(_startSector+i);
            crc^=buf[i];
        }
        sendBuf(buf);
        sendByte(crc);
        
        if (waitForACK()==false) return false;
    
        debug.log(DEBUG_MESSAGE, "Write protection enabled successfull");
        return true;
    }
    
    public boolean writeUnprotect() throws Exception {
        
        debug.log(DEBUG_VISUAL, "--------------------");
        debug.log(DEBUG_API, "Disabling write protection");
        
        if (bootCmd.cmdWriteUnprotect.sendCmd()==false) return false;
   
        debug.log(DEBUG_MESSAGE, "Disabling started");
        
        if (waitForACK(ERASE_WAIT_DELAY,-1)==false) return false;
        
        debug.log(DEBUG_MESSAGE, "Write protection disabled successfull");
        return true;
    }
    
    
    public boolean writeAll(int _startAdr, byte[] _data) throws Exception {
        int len = _data.length;
        int align = len%4;
        if (align>0) len+=(4-align);
        int tmp;
            
        debug.log(DEBUG_API, "Writing data to memory");
        
        int endAdr=_startAdr+len;
        byte buf[] = new byte[len];
        Arrays.fill(buf, (byte)0xFF);
        System.arraycopy(_data, 0, buf, 0, _data.length);
        
        for (int adr=_startAdr, off=0; adr<endAdr; adr+=WRITE_PACKET_SIZE, off+=WRITE_PACKET_SIZE) {
            debug.log(DEBUG_API, String.format("Writing progress %d%%", off*100/len));
            if (endAdr-adr>=WRITE_PACKET_SIZE) tmp=WRITE_PACKET_SIZE; else tmp=(int)(endAdr-adr);
            byte sendBuf[] = new byte[tmp];
            System.arraycopy(buf, off, sendBuf, 0, tmp);
            if (write((int)adr, sendBuf)==false) {
                debug.error(DEBUG_API, String.format("Writing error at 0x%08X", adr));
                return false;
            }
        }
        debug.log(DEBUG_API, "Writing successfull");
        return true;
    }
    
    public boolean readAll(int _startAdr, byte[] _data, int _len) throws Exception {

        int tmp;
            
        debug.log(DEBUG_API, "Reading data from memory");
        
        int endAdr=_startAdr+_len;
       
        for (int adr=_startAdr, off=0; adr<endAdr; adr+=READ_PACKET_SIZE, off+=READ_PACKET_SIZE) {
            debug.log(DEBUG_API, String.format("Reading progress %d%%", off*100/_len));
            if (endAdr-adr>=READ_PACKET_SIZE) tmp=READ_PACKET_SIZE; else tmp=(int)(endAdr-adr);
            byte recvBuf[] = new byte[tmp];
            if (read((int)adr, recvBuf, tmp)==false) {
                debug.error(DEBUG_API, String.format("Reading error at 0x%08X", adr));
                return false;
            }
            System.arraycopy(recvBuf, 0, _data, off, tmp);
        }
        debug.log(DEBUG_API, "Reading successfull");
        return true;
    }
    
    public boolean verifyAll(int _startAdr, byte[] _data, int _len) throws Exception {

        int tmp;
            
        debug.log(DEBUG_API, "Veryfing data in memory");
        
        int endAdr=_startAdr+_len;
       
        for (int adr=_startAdr, off=0; adr<endAdr; adr+=READ_PACKET_SIZE, off+=READ_PACKET_SIZE) {
            debug.log(DEBUG_API, String.format("Veryfing progress %d%%", off*100/_len));
            if (endAdr-adr>=READ_PACKET_SIZE) tmp=READ_PACKET_SIZE; else tmp=(int)(endAdr-adr);
            byte recvBuf[] = new byte[tmp];
            if (read((int)adr, recvBuf, tmp)==false) {
                debug.error(DEBUG_API, String.format("Reading error at 0x%08X", adr));
                return false;
            }
            for (int i=0; i<tmp; i++) {
                if (recvBuf[i]!=_data[off+i]) {
                    debug.error(DEBUG_API, String.format("Veryfing error at 0x%08X", adr+i));
                    return false;
                }
            }
        }
        debug.log(DEBUG_API, "Veryfing successfull");
        return true;
    }
    
    
    public boolean go(int _adr) throws Exception {
        debug.log(DEBUG_VISUAL, "--------------------");
        debug.log(DEBUG_API, String.format("Running program at 0x%08X ",_adr));
        
        if (bootCmd.cmdGo.sendCmd()==false) return false;
           
        debug.log(DEBUG_MESSAGE, "Uploading address");
        
        sendBuf(new byte[] {(byte)(_adr>>24), (byte)(_adr>>16), (byte)(_adr>>8), (byte)(_adr>>0), (byte)((_adr>>24)^(_adr>>16)^(_adr>>8)^(_adr>>0))});
       
        if (waitForACK()==false) return false;
    
        debug.log(DEBUG_API, "Running successfull");
        
        return true;
    }
    
    public boolean connect() throws Exception {
        int i;
        
        debug.log(DEBUG_VISUAL, "--------------------");
        debug.log(DEBUG_API, "Trying connect to STM32 UART bootloader");
        
        if (waitForACK(CONNECT_WAIT_DELAY,0x7F)==false) {
            debug.error(DEBUG_ERROR, "Cannot connect to STM32 UART bootloader");
            return false;
        }
        
        debug.log(DEBUG_API, "STM32 UART bootloader connected");
        
        if (getVersionAndCommands()==false) {
            debug.error(DEBUG_ERROR, "Cannot get bootloader version and commands");
            return false;
        }
        
        if (getID()==false) {
            debug.error(DEBUG_ERROR, "Cannot get product ID");
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean waitForACK(int _n, int _send) throws IOException {
        int data;
        while (_n-->0) {
            debug.log(DEBUG_API, "Waiting... ["+_n+"s]");
            if (_send!=-1) sendByte(_send);
            data=recvByte();
            if ((byte)data==BootAPI.ACK) return true;
        }
        return false;
    }
    
    @Override
    public boolean waitForACK() throws IOException {
        int data;
        data=recvByte();
        if ((byte)data==BootAPI.ACK) return true;
        return false;
    }
    
    @Override
    public void sendByte(int _b) throws IOException {
        outStream.write((byte)_b&0xFF);
        outStream.flush();
        debug.log(DEBUG_SERIALPORT, String.format("TX: %02X",_b&0xFF));
    }
    
    @Override
    public int recvByte() throws IOException {
        int data;
        data=inStream.read();
        if (data!=-1) debug.log(DEBUG_SERIALPORT, String.format("RX: %02X",data&0xFF));
        return data;
    }
    
    @Override
    public void sendCmdRequest(BootCMD _cmd) throws IOException {
        debug.log(DEBUG_MESSAGE, "Sending CMD <"+_cmd.getName()+">");
        sendBuf(new byte[] {(byte)_cmd.getCmd(), (byte)~_cmd.getCmd()});
    }

    @Override
    public void sendBuf(byte[] _b) throws IOException {
        int len=_b.length;
        outStream.write(_b, 0, len);
        outStream.flush();
        StringBuilder sb = new StringBuilder();
        sb.append("TX: ");       
        for (int i=0; i<len; i++) sb.append(String.format("%02X " ,_b[i]&0xFF));
        debug.log(DEBUG_SERIALPORT, sb.toString());
    }
}
