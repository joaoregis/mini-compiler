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
        
        String program;
        try (Scanner input = new Scanner(System.in)) {
            program = input.nextLine();
        }
        Compiler b = new Compiler(program);
        b.init();
        
        System.out.println(b.getError());
        System.out.println(b.getCodeObjeto());
    }
    
}
