/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bootstm32;

import gnu.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author marcin
 */
public class BootSerialPort {
    private OutputStream outStream;
    private InputStream inStream;
    private SerialPort serialPort;
    private CommPort commPort;
    
    public void connect ( String portName, int _speed) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(_speed,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_EVEN);
                serialPort.enableReceiveTimeout(1000);
                inStream = serialPort.getInputStream();
                outStream = serialPort.getOutputStream();

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    public void disconnect() throws IOException {
        inStream.close();
        outStream.close();
        serialPort.close();
    }
    
    public InputStream getInputStream() {
        return inStream;
    }
    public OutputStream getOutputStream() {
        return outStream;
    }
}
