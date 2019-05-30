package br.com.joaoregis.compiler;

import java.util.Scanner;

/**
 *
 * @author jooh_
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Scanner input = new Scanner(System.in);

        String programaFonte = input.nextLine();

        input.close();

        Compiler compBerco = new Compiler(programaFonte);

        compBerco.init();
        compBerco.program(); 

        System.out.println(compBerco.getCodeObjeto());
    }
    
}
