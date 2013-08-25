/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bootstm32;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 * @author marcin
 */
public class MyDebug {
    private PrintStream out;
    private PrintStream outError;
    private String key;
    private int level;
    public MyDebug(PrintStream _o, PrintStream _oe, String _key, int _level) {
        out=_o;
        outError=_oe;
        key=_key;     
        level=_level;
    }
    public void setLevel(int _l) {
        level=_l;
    }
    public void log(int _level, String _s) {
        if (out!=null && _level>=level) out.println(System.currentTimeMillis()+" ["+key+"]: "+_s);
    }
    public void error(int _level, String _s) {
        if (outError!=null && _level>=level) outError.println(System.currentTimeMillis()+" ["+key+"]: "+_s);
    }
}
