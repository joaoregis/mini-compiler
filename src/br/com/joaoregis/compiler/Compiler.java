package br.com.joaoregis.compiler;

/**
 *
 * @author jooh_
 */
public class Compiler {
    private char token;
    private int pos_token;
    private char program[];
    private String error;
    private String codeObjeto;
    
    public String getError() {
        return this.error;
    }
    
    public String getCodeObjeto() {
        return this.codeObjeto;
    }
    
    public Compiler(String program) {
        this.program = program.toCharArray();
        this.pos_token = -1;
        this.codeObjeto = "";
        this.error = "";
    }
    
    private void nextChar() {
        this.pos_token++;
        if (this.pos_token < program.length) {
            token = program[this.pos_token];
        }
    }
    
    public void init() {
        nextChar();
        expression();
    }

    private void match(char c) {
        if (token != c) {
            error += "[ERROR] " + c + " was expected\n";
        }
        
        nextChar();
    }
    
    private char getName() {
        char name;
        if (!Character.isLetter(token)) {
            error += "[ERROR] Name was expected\n";
        }
        
        name = Character.toUpperCase(token);
        nextChar();
        
        return name;
    }
    
    private char getNum() {
        char num;
        if (!Character.isDigit(token)) {
            error += "[ERROR] Num was expected\n";
        }
        
        num = token;
        nextChar();
        
        return num;
    }
    
    private void emit(String pcode) {
        codeObjeto += pcode + "\n";
    }
    
    private void expression() {
        term();
        while(isAddOp(token)) {
            emit("PUSH AX");
            switch(token) {
                case '+': {
                    add();
                    break;
                }
                case '-': {
                    subtract();
                    break;
                }
                default: {
                    error += "[ERROR] AddOp was expected\n";
                    break;
                }
            }
        }
        
        if (token != 'e') {
            error += "[ERROR] End was expected\n";
        }
    }
    
    private boolean isAddOp(char c) {
        String op = "+-";
        return (c == '+' || c == '-');
    }
    
    private void add() {
        match('+');
        term();
        emit("POP BX");
        emit("ADD AX, BX");
    }
   
    private void subtract() {
        match('-');
        term();
        emit("POP BX");
        emit("SUB AX, BX");
        emit("NEG AX");
    }
    
    private boolean isMulOp(char c) {
        return (c == '*' || c == '/');
    }
    
    private void term() {
        factor();
        while(isMulOp(token)) {
            emit("PUSH AX");
            switch(token) {
                case '*': {
                    multiply();
                    break;
                }
                case '/': {
                    divide();
                    break;
                }
                default: {
                    error += "[ERROR] MulOp was expected\n";
                    break;
                }
            }
        }
    }
    
    private void multiply() {
        match('*');
        factor();
        emit("POP BX");
        emit("IMUL BX");
    }
    
    private void divide() {
        match('/');
        factor();
        emit("POP BX");
        emit("XCHG AX, BX");
        emit("CWD");
        emit("IDIV BX");
    }
    
    private void factor() {
        emit("MOV AX, " + getNum());
    }
            
    
}
