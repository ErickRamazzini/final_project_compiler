import java.util.List;

public class Parser {
    private List<Token> tokens;
    private int pos;
    private ValidationResult result;

    public SelectStatement parse(List<Token> tokens, ValidationResult result) {
        this.tokens = tokens;
        this.pos = 0;
        this.result = result;
        SelectStatement statement = new SelectStatement();
        expect(TokenType.SELECT, "SYNTACTIC_EXPECTED_SELECT");
        parseColumns(statement);
        expect(TokenType.FROM, "SYNTACTIC_EXPECTED_FROM");
        Token table = expect(TokenType.IDENTIFIER, "SYNTACTIC_EXPECTED_TABLE");
        if (table != null) statement.table = table.lexeme;

        // TODO SERIE 2:
        // Implementar parseo de WHERE opcional:
        // WHERE <columna> <operador> <literal> (AND|OR <columna> <operador> <literal>)*
        // Debe llenar statement.where con SourceSpan exactos.

        if (match(TokenType.WHERE)) {
    ConditionChain   chain =new ConditionChain();
    parseWhereCondition(chain);
    while  (check(TokenType.AND) || check(TokenType.OR)) {
        String connector= current().lexeme.toUpperCase();
        advance();
        chain.connectors.add(connector);
        parseWhereCondition(chain);
    }
    statement.where = chain;
}


        if (check(TokenType.SEMICOLON)) advance();
        if (!check(TokenType.EOF)) {
            result.diagnostics.add(new Diagnostic("SYNTACTIC_UNEXPECTED_TOKEN", "Token inesperado: " + current().lexeme, current().span));
        }
        return statement;
    }

    private void parseColumns(SelectStatement statement) {
        if (match(TokenType.STAR)) {
            statement.columns.add("*");
            return;
        }
        Token first = expect(TokenType.IDENTIFIER, "SYNTACTIC_EXPECTED_COLUMN");
        if (first != null) statement.columns.add(first.lexeme);
        while (match(TokenType.COMMA)) {
            Token next = expect(TokenType.IDENTIFIER, "SYNTACTIC_EXPECTED_COLUMN");
            if (next != null) statement.columns.add(next.lexeme);
        }
    }

    private Token expect(TokenType type, String code) {
        if (check(type)) return advance();
        result.diagnostics.add(new Diagnostic(code, "Se esperaba " + type + " y se encontró " + current().type, current().span));
        return null;
    }

    private boolean match(TokenType type) { if (check(type)) { advance(); return true; } return false; }
    private boolean check(TokenType type) { return current().type == type; }
    private Token current() { return tokens.get(pos); }
    private Token advance() { return tokens.get(pos++); }

    


    private void parseWhereCondition(ConditionChain chain) {

    Token col = expect(TokenType.IDENTIFIER, "SYNTACTIC_EXPECTED_COLUMN");
    if (col == null) return;
    Token op = expectOperator();
    if (op == null) return;
    Token lit = expectLiteral();
    if (lit == null) return;
    chain.conditions.add(new WhereCondition(
        col.lexeme, op.lexeme, lit.lexeme, classifyLiteral(lit),
        col.span, op.span, lit.span));

}

private Token expectOperator() {

    TokenType t = current().type;
    if (t == TokenType.EQUAL || t ==TokenType.GREATER || t == TokenType.LESS ||
        t == TokenType.GREATER_EQUAL || t== TokenType.LESS_EQUAL || t == TokenType.NOT_EQUAL)
        return advance();
    result.diagnostics.add(new Diagnostic("SYNTATIC_EXPECTED_WHERE_OPERAND",
        "operador y se encontró " + current().type, current().span));
    return null;

}

private Token expectLiteral() {
    TokenType t = current().type;
    if (t ==TokenType.NUMBER || t== TokenType.STRING  ||

        t ==TokenType.TRUE || t == TokenType.FALSE)
        return advance();
    result.diagnostics.add(new Diagnostic("SYNTACTIC_EXPECTED_WHERE_OPERAND",
        "Se esperaba letra y se encontró " +current().type, current().span));
    return null;

}

private LiteralType classifyLiteral(Token t) {

    if (t.type ==TokenType.NUMBER) return LiteralType.NUMBER;
    if (t.type == TokenType.STRING) return LiteralType.STRING;
    return LiteralType.BOOLEAN;
}

}
