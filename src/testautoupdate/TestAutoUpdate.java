/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package testautoupdate;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fakhruzzaman
 */
public class TestAutoUpdate {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
         try {
             
            AutoUpdate au = new AutoUpdate();
            
            au.update();
            
             
            
        } catch (IOException ex) {
            Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        
    }
    
    public static void runECSS() {
        try {

            String pathCSS = AutoUpdate.getData(2) + "CSS/CSS/";
            String path = pathCSS + "CSS.jar";
//            System.out.println("path:" + path);
//            System.out.println("cp " + pathCSS + "ipcall .");
//            Runtime.getRuntime().exec("cp " + pathCSS + "ipcall .");
//            Runtime.getRuntime().exec("cp -r " + pathCSS + "db .");
            Runtime.getRuntime().exec("java -jar " + path);

            // MainJFrame au = new MainJFrame();
            //   au.setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(TestAutoUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
