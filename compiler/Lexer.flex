// Created for COE 1622 project1
// Built off the example provided in the JFlex manual
// author: Tyler Mohnke

package compiler;

import java_cup.runtime.*;
import compiler.syntaxtree.Identifier;

%%

%public
%class Lexer
%cup
%line
%column

%{
    private Symbol symbol(int type)
    {
        return new Symbol(type, yyline+1, yycolumn+1);
    }

    private Symbol symbol(int type, Object value)
    {
        return new Symbol(type, yyline+1, yycolumn+1, value);
    }
%}

LineTerminator      = \r|\n|\r\n
InputCharacter      = [^\r\n]
WhiteSpace          = {LineTerminator} | [ \t\f]

TraditionalComment  = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment    = "//" {InputCharacter}* {LineTerminator}?
Comment             = {TraditionalComment} | {EndOfLineComment}

Identifier          = [a-zA-Z_][a-zA-Z0-9_]*

DecIntegerLiteral   = 0 | [1-9][0-9]*
HexIntegerLiteral   = 0[xX][0-9a-fA-F]*
OctIntegerLiteral   = 0[0-7]*
IntegerLiteral      = {DecIntegerLiteral} | {HexIntegerLiteral} | {OctIntegerLiteral}

%%

<YYINITIAL>
{
    // Keywords
    "String"                { return symbol(sym.STRING); }
    "System.out.println"    { return symbol(sym.PRINTLN); }
    "boolean"               { return symbol(sym.BOOLEAN); }
    "class"                 { return symbol(sym.CLASS); }
    "else"                  { return symbol(sym.ELSE); }
    "extends"               { return symbol(sym.EXTENDS); }
    "false"                 { return symbol(sym.FALSE); }
    "if"                    { return symbol(sym.IF); }
    "int"                   { return symbol(sym.INT); }
    "length"                { return symbol(sym.LENGTH); }
    "main"                  { return symbol(sym.MAIN); }
    "new"                   { return symbol(sym.NEW); }
    "public"                { return symbol(sym.PUBLIC); }
    "return"                { return symbol(sym.RETURN); }
    "static"                { return symbol(sym.STATIC); }
    "this"                  { return symbol(sym.THIS); }
    "true"                  { return symbol(sym.TRUE); }
    "void"                  { return symbol(sym.VOID); }
    "while"                 { return symbol(sym.WHILE); }
    
    // Operators
    "!"                     { return symbol(sym.NOT); }
    "&&"                    { return symbol(sym.AND); }
    "*"                     { return symbol(sym.MULT); }
    "+"                     { return symbol(sym.PLUS); }
    "–"                     { return symbol(sym.MINUS); }
    "-"                     { return symbol(sym.MINUS); }
    "<"                     { return symbol(sym.LESSTHAN); }
    "="                     { return symbol(sym.ASSIGNMENT); }
    
    // Misc
    "."                     { return symbol(sym.DOT); }
    ","                     { return symbol(sym.COMMA); }
    ";"                     { return symbol(sym.SEMICOLON); }
    "{"                     { return symbol(sym.LBRACE); }
    "}"                     { return symbol(sym.RBRACE); }
    "("                     { return symbol(sym.LPAREN); }
    ")"                     { return symbol(sym.RPAREN); }
    "["                     { return symbol(sym.LBRACKET); }
    "]"                     { return symbol(sym.RBRACKET); }
    
    {IntegerLiteral}        { return symbol(sym.INTEGER, Integer.decode(yytext())); }
    {Identifier}            { return symbol(sym.ID, new Identifier(yytext())); }
    
    {Comment}               { }
    {WhiteSpace}            { }
}


// error fallback
[^]                         { System.err.format("Illegal character '%c' at line %d column %d\n", yytext().charAt(0), yyline + 1, yycolumn + 1); }

<<EOF>>                     { return symbol(sym.EOF); }    

