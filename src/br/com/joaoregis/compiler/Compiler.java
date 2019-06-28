package br.com.joaoregis.compiler;

public class Compiler {

    private char token;
    private int pos_token;
    private char program[];
    private String error;
    private String codeObjeto;
    private int labelCount;

    public String getError() {
        return error;
    }

    public void setProgram(char[] program) {
        this.program = program;
    }

    public String getCodeObjeto() {
        return codeObjeto;
    }

    public Compiler(String pProgram) {
        program = pProgram.toCharArray();
        pos_token = -1;
        this.codeObjeto = "";
        this.labelCount = 0;
    }

    public void nextChar() {
        
        this.pos_token++;

        while (
            (pos_token < program.length) &&
            (program[this.pos_token] == ' ' 
            || program[this.pos_token] == '\r' 
            || program[this.pos_token] == '\t' 
            || program[this.pos_token] == '\n')
        ) {

            this.pos_token++;

        }

        if (pos_token < program.length) {
            token = program[pos_token];

        }
    }

    public void init() {
        nextChar();
    }

    public void match(char c) {
        if (token != c) {
            error += c + " esperado";
        }
        nextChar();
    }

    public char getName() {
        char name;
        if (!Character.isLetter(token)) {
            error += "Name esperado";
        }

        name = Character.toUpperCase(token);
        nextChar();
        return name;
    }

    public char getNum() {
        char num;
        if (!Character.isDigit(token)) {
            error += "Integer esperado";
        }
        num = token;
        nextChar();
        return num;
    }

    public void emit(String pCode) {
        codeObjeto += pCode + "\n";
    }

    public void expression() {
        term();
        while (isAddOp(token)) {
            emit("PUSH AX");
            switch (token) {
                case '+':
                    add();
                    break;
                case '-':
                    subtract();
                    break;
                default:
                    error += "AddOp esperado";
                    break;
            }
        }

        if (token != 'e') {
            error += "End esperado";
        }
    }

    public boolean isAddOp(char c) {
        return (c == '+' || c == '-');
    }

    public void add() {
        match('+');
        term();
        emit("POP BX");
        emit("ADD AX,BX");
    }

    public void subtract() {
        match('-');
        term();
        emit("POP BX");
        emit("SUB AX,BX");
        emit("NEG AX");
    }

    public void term() {
        factor();
        while (token == '*' || token == '/') {
            emit("PUSH AX");
            switch (token) {
                case '*':
                    multiply();
                    break;
                case '/':
                    divide();
                    break;
                default:
                    error += "MulOp esperado";
                    break;
            }
        }
    }

    public void multiply() {
        match('*');
        factor();
        emit("POP BX");
        emit("IMUL BX");
    }

    public void divide() {
        match('/');
        factor();
        emit("POP BX");
        emit("XCHG AX, BX");
        emit("CWD");
        emit("IDIV BX");
    }

    public void factor() {
        if (token == '(') {
            match('(');
            //expression();
            boolExpression();
            match(')');
        } else if (Character.isLetter(token)) {
            emit("MOV AX, [" + getName() + "]");
        } else {
            emit("MOV AX, " + getNum());
        }

    }

    public void assignment() {
        char name;
        name = getName();
        match('=');
        boolExpression();
        emit("MOV [" + name + "], AX");
    }

    void block() {
        while (token != 'e' && token != 'l') {
            switch (token) {
                case 'i':
                    doIf();
                    break;
                    
                case 'w':
                    doWhile();
                    break;
                    
                default:
                    assignment();
                    break;
            }
        }
    }

    void program() {
        block();
        if (token != 'e') {
            error += "END esperado";
        }
        emit("END");
    }

    int newLabel() {
        return labelCount++;
    }

    void postLabel(int lbl) {
        emit("L" + lbl + ":");
    }
    
    void doWhile() {
        
        int l1, l2;
        match('w');
        l1 = newLabel();
        l2 = newLabel();
        postLabel(l1);
        boolExpression();
        emit("JZ L" + l2);
        block();
        match('e');
        emit("JMP L" + l1);
        postLabel(l2);
        
    }

    void doIf() {
        int l1, l2;
        match('i'); // IF
        l1 = newLabel();
        boolExpression();
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
        match('e'); // ENDIF
        postLabel(l2);
    }

    boolean isBoolean(char c) {
        return (c == 'T' || c == 'F');
    }

    boolean getBoolean() {
        boolean b;

        if (!isBoolean(token)) {
            error = "Boolean esperado";
        }

        b = (token == 'T');
        nextChar();
        return b;
    }

    void boolOr() {
        match('|');
        boolTerm();
        emit("POP BX");
        emit("OR AX, BX");
    }

    void boolXor() {
        match('~');
        boolTerm();
        emit("POP BX");
        emit("XOR AX, BX");
    }

    void boolExpression() {
        boolTerm();

        while (isOrOp(token)) {
            emit("PUSH AX");
            switch (token) {
                case '|':
                    boolOr();
                    break;

                case '~':
                    boolXor();
                    break;

                default:
                    break;
            }
        }
    }

    boolean isOrOp(char c) {
        return (c == '|' || c == '~');
    }

    void boolTerm() {
        notFactor();

        while (token == '&') {
            emit("PUSH AX");
            match('&');
            notFactor();
            emit("POP BX");
            emit("AND AX, BX");
        }
    }

    void notFactor() {
        if (token == '!') {
            match('!');
            boolFactor();
            emit("NOT BX");
        } else {
            boolFactor();
        }
    }

    void boolFactor() {
        if (isBoolean(token)) {
            if (getBoolean()) {
                emit("MOV AX, -1");
            } else {
                emit("MOV AX, 0");
            }
        } else {
            relaction();
        }
    }

    void relaction() {
        expression();
        if (isRelOp(token)) {
            emit("PUSH AX");

            switch (token) {
                case '=':
                    equals();
                    break;

                case '<':
                    minor();
                    break;

                case '>':
                    higher();
                    break;

                default:
                    break;
            }
        }
    }

    boolean isRelOp(char c) {
        return (c == '=' || c == '#' || c == '<' || c == '>');
    }

    void equals() {
        int l1, l2;

        match('=');
        l1 = newLabel();
        l2 = newLabel();
        expression();
        emit("POP BX");
        emit("CMP BX, AX");
        emit("JE L" + l1);
        emit("MOV AX, 0");
        emit("JMP L" + l2);
        postLabel(l1);
        emit("MOV AX, -1");
        postLabel(l2);
    }

    void minor() {
        int l1, l2;

        match('=');
        l1 = newLabel();
        l2 = newLabel();
        expression();
        emit("POP BX");
        emit("CMP BX, AX");
        emit("JL L" + l1);
        emit("MOV AX, 0");
        emit("JMP L" + l2);
        postLabel(l1);
        emit("MOV AX, -1");
        postLabel(l2);
    }
    
    void higher() {
        int l1, l2;
        match('=');
        l1 = newLabel();
        l2 = newLabel();
        expression();
        emit("POP BX");
        emit("CMP BX, AX");
        emit("JG L" + l1);
        emit("MOV AX, 0");
        emit("JMP L" + l2);
        postLabel(l1);
        emit("MOV AX, -1");
        postLabel(l2);
    }

}
