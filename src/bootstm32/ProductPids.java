/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bootstm32;

import java.util.ArrayList;

/**
 *
 * @author marcin
 */
public class ProductPids {
    private ArrayList<ProductPID> listPids; 
    public ProductPids() {
        listPids = new ArrayList<>();
        
        listPids.add(new ProductPID(0x412,"STM32F1 - low density"));
        listPids.add(new ProductPID(0x410,"STM32F1 - medium density"));
        listPids.add(new ProductPID(0x414,"STM32F1 - high density"));
        listPids.add(new ProductPID(0x418,"STM32F1 - connectivity line"));
        listPids.add(new ProductPID(0x420,"STM32F1 - medium density value line"));
        listPids.add(new ProductPID(0x428,"STM32F1 - high density value line"));
        listPids.add(new ProductPID(0x430,"STM32F1 - XL density"));
        
        listPids.add(new ProductPID(0x416,"STM32L1 - medium density"));
        listPids.add(new ProductPID(0x436,"STM32L1 - high density"));
        listPids.add(new ProductPID(0x427,"STM32L1 - medium density plus"));
        
        listPids.add(new ProductPID(0x411,"STM32F2xxxx"));
        
        listPids.add(new ProductPID(0x440,"STM32F051xx"));
        listPids.add(new ProductPID(0x444,"STM32F050xx"));
        
        listPids.add(new ProductPID(0x413,"STM32F40xxx/41xxx"));
        listPids.add(new ProductPID(0x419,"STM32F427xx/437xx/429xx/439xx"));
        
        listPids.add(new ProductPID(0x432,"STM32F37xxx/38xxx"));
        listPids.add(new ProductPID(0x422,"STM32F30xxx/31xxx"));
    }
    
    public String getProductName(int _p) {
        for (ProductPID pid : listPids) {
            if (_p==pid.getPID()) return pid.getName();
        }
        return "Unknown product";
    }
}
