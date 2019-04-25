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
    private int labelCount;
    
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
        this.labelCount = 0;
    }
    
    private void nextChar() {
        this.pos_token++;
        
        while (
            program[this.pos_token] == ' ' 
            || program[this.pos_token] == '\r' 
            || program[this.pos_token] == '\t' ) {

            this.pos_token++;

            if (this.pos_token >= program.length)
                break;

        }
        
        if (this.pos_token < program.length) {
            token = program[this.pos_token];
        }
    }
    
    public void init() {
        nextChar();
        program();
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
            
    private void assignment() {
        char name;
        name = getName();
        match('=');
        expression();
        emit("MOV [" + name + "], AX");
    }
    
    private void program() {
        block();
        if (token != 'e')
            error += "[ERROR] End was expected\n";
        
        emit("END");
    }
    
    private void block() {
        while(token != 'e' && token != 'l') {
            switch(token) {
                case 'i':
                    doIf();
                    break;
                    
                default:
                    other();
                    break;
            }
        }
    }
    
    private void other() {
        emit("# " + getName());
    }
    
    private void condition() {
        emit("# condition");
    }
    
    private int newLabel() {
        return labelCount++;
    }
    
    private void postLabel(int lbl) {
        emit("L" + lbl + ":");
    }
    
    private void doIf() {
        int l1, l2;
        match('i');
        condition();
        l1 = newLabel();
        l2 = l1;
        emit("JZ L" + l1);
        block();
        
        if (token == 'l') {
            match('l');
            l2 = newLabel();
            emit("JMP L" + l2);
            postLabel(l1);
            block();
        }

        match('e');
        postLabel(l2);
    }
    
}
