/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bootstm32;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 *
 * @author marcin
 */
public class HexToBin {
    private long startAddress;
    private long endAddress;
    private long lengthData;
    private byte[] buffer;
    
    public HexToBin() {
        startAddress = 0xFFFFFFFFL;
        endAddress= 0x00000000L;
        lengthData= 0;
    }
    
    public long getStartAddress() {
        return startAddress;
    }
    public long getEndAddress() {
        return endAddress;
    }
    
    public byte[] convert(String _s) throws FileNotFoundException, IOException, Exception {
        
        InputStream is = new FileInputStream(_s);
        IntelHexParser ihp = new IntelHexParser(is);
        ihp.setDataListener(new IntelHexDataListener() {
            @Override
            public void data(long address, byte[] data) {
                int length = data.length;
                if (address<startAddress) startAddress=address;
                if (address+length-1>endAddress) endAddress=address+length-1;
            }

            @Override
            public void eof() {
            }
        });
        ihp.parse();
        is.close();
                
        if (startAddress>endAddress) return null;
        lengthData=endAddress-startAddress+1;
        buffer = new byte[(int)lengthData];
        Arrays.fill(buffer,(byte)0xFF);
        
        is = new FileInputStream(_s);
        ihp = new IntelHexParser(is);
        
        ihp.setDataListener(new IntelHexDataListener() {
            @Override
            public void data(long address, byte[] data) {
                int length = data.length;
                System.arraycopy(data, 0, buffer, (int) (address - startAddress), length);
            }

            @Override
            public void eof() {
            }
        });
        ihp.parse();
        
        is.close();
        return buffer;
    }
    
}
