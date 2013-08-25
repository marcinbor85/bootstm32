/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bootstm32;

/**
 *
 * @author marcin
 */
public class ProductPID {
    private int pid;
    private String name;
    public ProductPID(int _p, String _name) {
        pid=_p;
        name=_name;
    }
    public String getName() {
        return name;
    }
    public int getPID() {
        return pid;
    }
}
