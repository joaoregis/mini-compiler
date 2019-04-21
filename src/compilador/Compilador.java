/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.util.Scanner;

/**
 *
 * @author jooh_
 */
public class Compilador {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Scanner input = new Scanner(System.in);
        String program = input.nextLine();
        input.close();
        Berco b = new Berco(program);
        b.init();
        
        System.out.println(b.getError());
        System.out.println(b.getCodeObjeto());
    }
    
}
