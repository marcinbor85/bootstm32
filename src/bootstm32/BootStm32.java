/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bootstm32;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class BootStm32 {
    
    private static BootAPI boot;
    private HashMap bootOptionsMap;
    private ArrayList<BootAction> bootActionsList;
    private BootSerialPort serialPort;
    private static byte[] dataFirmware;
    private static int startAddress;
    private static int startProtectSector;
    private static int lengthProtectSector;

    
    public BootStm32(HashMap _map, ArrayList<BootAction> _list) throws Exception {
        bootOptionsMap=_map;
        bootActionsList=_list;
                   
        serialPort = new BootSerialPort();
        serialPort.connect((String)bootOptionsMap.get("port"),  (Integer)bootOptionsMap.get("boudrate"));
        HexToBin h2b = new HexToBin();
        if (bootOptionsMap.containsKey("file")) {
            dataFirmware = h2b.convert((String)bootOptionsMap.get("file")); 
            startAddress= (int)h2b.getStartAddress();
        }
        
        boot = new BootAPI(serialPort.getOutputStream(),serialPort.getInputStream());
        boot.setDebugLevel((Integer)bootOptionsMap.get("debug"));
        
        if (bootOptionsMap.containsKey("startProtectSector")) startProtectSector=(Integer)bootOptionsMap.get("startProtectSector");
        if (bootOptionsMap.containsKey("lengthProtectSector")) lengthProtectSector=(Integer)bootOptionsMap.get("lengthProtectSector");
        
    }
    
    private void end() throws IOException {
        serialPort.disconnect();
        System.out.println("Error");
        System.exit(-1);
    }
    
    private void run() throws Exception {
        if (boot.connect()==false) end();
        
        for (BootAction act : bootActionsList) {
            if (act.go()==false) end();
        }
        
        System.out.println("All jobs finished");
        
        serialPort.disconnect();
        System.exit(0);
        
    }
    
    public static void printHelp() {
        System.out.println();
        System.out.println("STM32 Family USART Bootloader");
        System.out.println("Version 1.0.1, 2013.08.31");
        System.out.println("Marcin Borowicz, email: marcin_bor@wp.pl");
        System.out.println();
        System.out.println("Parameters (the order is not important):");
        System.out.println("    -p=<serial_port_name>                - default /dev/ttyUSB0");
        System.out.println("    -b=<boudrate>                        - default 115200");
        System.out.println("    -d=<debug_level>                     - default 20");
        System.out.println("    -f=<input_file_name_HEX>             - if nessesery");
        System.out.println("    -sps=<start_write_protect_sector>    - if nessesery");
        System.out.println("    -lps=<length_write_protect_sector>   - if nessesery");
        System.out.println();
        System.out.println("Actions (executed in the order of entry):");
        System.out.println("    -ru       - readout unprotect request");
        System.out.println("    -wu       - write unprotect request");
        System.out.println("    -e        - mass erase request");
        System.out.println("    -w        - uploading data write request");
        System.out.println("    -v        - verify request");
        System.out.println("    -wp       - write protect request");
        System.out.println("    -rp       - readout protect request");
        System.out.println("    -g        - set PC to the file address");
        System.out.println();
        System.out.println("TODO:");
        System.out.println(" 1. Erase selected sectors");
        System.out.println(" 2. Read from device to file");
        System.out.println(" 3. Selectable timeouts");
        System.out.println(" 4. Longer connection time");
        System.out.println(" 5. End line chars windows/linux");
        System.out.println(" 6. Write multiple files at once");
        System.out.println();
        
        System.exit(-1);
    }
   
    public static void main(String[] args) throws Exception {

        HashMap argsMap = new HashMap();
        argsMap.put("port", "/dev/ttyUSB0");
        argsMap.put("boudrate", 115200);
        argsMap.put("debug", 20);
        
        ArrayList<BootAction> actionsList= new ArrayList<>();
        
        for (String arg : args) {
            if (arg.equals("--help") || arg.equals("--h") || arg.equals("-help") || arg.equals("-h")) printHelp();
        }
            
        for (String arg : args) {
            if (arg.startsWith("-p=")) argsMap.put("port", arg.substring(3, arg.length()));
            if (arg.startsWith("-b=")) argsMap.put("boudrate", Integer.parseInt(arg.substring(3, arg.length())));
            if (arg.startsWith("-f=")) argsMap.put("file", arg.substring(3, arg.length()));
            if (arg.startsWith("-d=")) argsMap.put("debug", Integer.parseInt(arg.substring(3, arg.length())));
            if (arg.startsWith("-sps=")) argsMap.put("startProtectSector", Integer.parseInt(arg.substring(5, arg.length())));
            if (arg.startsWith("-lps=")) argsMap.put("lengthProtectSector", Integer.parseInt(arg.substring(5, arg.length())));
        }
        
        for (String arg : args) { 
            if (arg.equals("-ru")) actionsList.add(new BootAction() {
                @Override
                public boolean go() throws Exception {
                    if (boot.readoutUnprotect()==false) return false;
                    if (boot.connect()==false) return false;
                    return true;
                }});            
            if (arg.equals("-wu")) actionsList.add(new BootAction() {
                @Override
                public boolean go() throws Exception {
                    if (boot.writeUnprotect()==false) return false;
                    if (boot.connect()==false) return false;
                    return true;
                }});
            if (arg.equals("-e")) actionsList.add(new BootAction() {
                @Override
                public boolean go() throws Exception {
                    if (boot.eraseAll()==false) return false;
                    return true;
                }});            
            if (arg.equals("-w")) actionsList.add(new BootAction() {
                @Override
                public boolean go() throws Exception {
                    if (boot.writeAll(startAddress,dataFirmware)==false) return false;
                    return true;
                }});
            if (arg.equals("-v")) actionsList.add(new BootAction() {
                @Override
                public boolean go() throws Exception {
                    if (boot.verifyAll(startAddress,dataFirmware,dataFirmware.length)==false) return false;
                    return true;
                }});
            if (arg.equals("-wp")) actionsList.add(new BootAction() {
                @Override
                public boolean go() throws Exception {
                    if (boot.writeProtect(startProtectSector, lengthProtectSector)==false) return false;
                    if (boot.connect()==false) return false;
                    return true;
                }});
            if (arg.equals("-rp")) actionsList.add(new BootAction() {
                @Override
                public boolean go() throws Exception {
                    if (boot.readoutProtect()==false) return false;
                    if (boot.connect()==false) return false;
                    return true;
                }});        
            if (arg.equals("-g")) actionsList.add(new BootAction() {
                @Override
                public boolean go() throws Exception {
                    if (boot.go(startAddress)==false) return false;
                    return true;
                }});
            
            
            //////
            
            if (arg.equals("-wp")) {
                if (argsMap.containsKey("startProtectSector")==false || argsMap.containsKey("lengthProtectSector")==false) {
                    System.out.println("ERROR: You have to specify start and length write protect sectors");
                    System.exit(-1);
                }
            }
            if (arg.equals("-w") || arg.equals("-g")) {
                if (argsMap.containsKey("file")==false) {
                    System.out.println("ERROR: You have to specify HEX file name");
                    System.exit(-1);
                }
            }            
        }
        

        BootStm32 stm32 = new BootStm32(argsMap, actionsList);
        stm32.run();
    }
    
}
