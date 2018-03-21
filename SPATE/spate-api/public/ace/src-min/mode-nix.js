      shift(47);                    // '//'
      lookahead1W(265);             // Wildcard | EQName^Token | IntegerLiteral | DecimalLiteral | DoubleLiteral |
      whitespace();
      parse_RelativePathExpr();
      break;
    default:
      parse_RelativePathExpr();
    }
    eventHandler.endNonterminal("PathExpr", e0);
  }

  function try_PathExpr()
  {
    switch (l1)
    {
    case 46:                        // '/'
      shiftT(46);                   // '/'
      lookahead1W(286);             // Wildcard | EQName^Token | IntegerLiteral | DecimalLiteral | DoubleLiteral |
      switch (l1)
      {
      case 25:                      // EOF
      case 26:                      // '!'
      case 27:                      // '!='
      case 37:                      // ')'
      case 38:                      // '*'
      case 40:                      // '+'
      case 41:                      // ','
      case 42:                      // '-'
      case 49:                      // ':'
      case 53:                      // ';'
      case 57:                      // '<<'
      case 58:                      // '<='
      case 60:                      // '='
      case 61:                      // '>'
      case 62:                      // '>='
      case 63:                      // '>>'
      case 69:                      // ']'
      case 87:                      // 'by'
      case 99:                      // 'contains'
      case 205:                     // 'paragraphs'
      case 232:                     // 'sentences'
      case 247:                     // 'times'
      case 273:                     // 'words'
      case 279:                     // '|'
      case 280:                     // '||'
      case 281:                     // '|}'
      case 282:                     // '}'
        break;
      default:
        try_RelativePathExpr();
      }
      break;
    case 47:                        // '//'
      shiftT(47);                   // '//'
      lookahead1W(265);             // Wildcard | EQName^Token | IntegerLiteral | DecimalLiteral | DoubleLiteral |
      try_RelativePathExpr();
      break;
    default:
      try_RelativePathExpr();
    }
  }

  function parse_RelativePathExpr()
  {
    eventHandler.startNonterminal("RelativePathExpr", e0);
    parse_StepExpr();
    for (;;)
    {
      switch (l1)
      {
      case 26:                      // '!'
        lookahead2W(266);           // Wildcard | EQName^Token | IntegerLiteral | DecimalLiteral | DoubleLiteral |
        break;
      default:
        lk = l1;
      }
      if (lk != 25                  // EOF
       && lk != 27                  // '!='
       && lk != 37                  // ')'
       && lk != 38                  // '*'
       && lk != 40                  // '+'
       && lk != 41                  // ','
       && lk != 42                  // '-'
       && lk != 46                  // '/'
       && lk != 47                  // '//'
       && lk != 49                  // ':'
       && lk != 53                  // ';'
       && lk != 54                  // '<'
       && lk != 57                  // '<<'
       && lk != 58                  // '<='
       && lk != 60                  // '='
       && lk != 61                  // '>'
       && lk != 62                  // '>='
       && lk != 63                  // '>>'
       && lk != 69                  // ']'
       && lk != 70                  // 'after'
       && lk != 75                  // 'and'
       && lk != 79                  // 'as'
       && lk != 80                  // 'ascending'
       && lk != 81                  // 'at'
       && lk != 84                  // 'before'
       && lk != 87                  // 'by'
       && lk != 88                  // 'case'
       && lk != 89                  // 'cast'
       && lk != 90                  // 'castable'
       && lk != 94                  // 'collation'
       && lk != 99                  // 'contains'
       && lk != 105                 // 'count'
       && lk != 109                 // 'default'
       && lk != 113                 // 'descending'
       && lk != 118                 // 'div'
       && lk != 122                 // 'else'
       && lk != 123                 // 'empty'
       && lk != 126                 // 'end'
       && lk != 128                 // 'eq'
       && lk != 131                 // 'except'
       && lk != 137                 // 'for'
       && lk != 146                 // 'ge'
       && lk != 148                 // 'group'
       && lk != 150                 // 'gt'
       && lk != 151                 // 'idiv'
       && lk != 160                 // 'instance'
       && lk != 162                 // 'intersect'
       && lk != 163                 // 'into'
       && lk != 164                 // 'is'
       && lk != 172                 // 'le'
       && lk != 174                 // 'let'
       && lk != 178                 // 'lt'
       && lk != 180                 // 'mod'
       && lk != 181                 // 'modify'
       && lk != 186                 // 'ne'
       && lk != 198                 // 'only'
       && lk != 200                 // 'or'
       && lk != 201                 // 'order'
       && lk != 205                 // 'paragraphs'
       && lk != 220                 // 'return'
       && lk != 224                 // 'satisfies'
       && lk != 232                 // 'sentences'
       && lk != 236                 // 'stable'
       && lk != 237                 // 'start'
       && lk != 247                 // 'times'
       && lk != 248                 // 'to'
       && lk != 249                 // 'treat'
       && lk != 254                 // 'union'
       && lk != 266                 // 'where'
       && lk != 270                 // 'with'
       && lk != 273                 // 'words'
       && lk != 279                 // '|'
       && lk != 280                 // '||'
       && lk != 281                 // '|}'
       && lk != 282                 // '}'
       && lk != 23578               // '!' '/'
       && lk != 24090)              // '!' '//'
      {
        lk = memoized(3, e0);
        if (lk == 0)
        {
          var b0A = b0; var e0A = e0; var l1A = l1;
          var b1A = b1; var e1A = e1; var l2A = l2;
          var b2A = b2; var e2A = e2;
          try
          {
            switch (l1)
            {
            case 46:                // '/'
              shiftT(46);           // '/'
              break;
            case 47:                // '//'
              shiftT(47);           // '//'
              break;
            default:
              shiftT(26);           // '!'
            }
            lookahead1W(265);       // Wildcard | EQName^Token | IntegerLiteral | DecimalLiteral | DoubleLiteral |
            try_StepExpr();
            lk = -1;
          }
          catch (p1A)
          {
            lk = -2;
          }
          b0 = b0A; e0 = e0A; l1 = l1A; if (l1 == 0) {end = e0A;} else {
          b1 = b1A; e1 = e1A; l2 = l2A; if (l2 == 0) {end = e1A;} else {
          b2 = b2A; e2 = e2A; end = e2A; }}
          memoize(3, e0, lk);
        }
      }
      if (lk != -1
       && lk != 46                  // '/'
       && lk != 47)                 // '//'
      {
        break;
      }
      switch (l1)
      {
      case 46:                      // '/'
        shift(46);                  // '/'
        break;
      case 47:                      // '//'
        shift(47);                  // '//'
        break;
      default:
        shift(26);                  // '!'
      }
      lookahead1W(265);             // Wildcard | EQName^Token | IntegerLiteral | DecimalLiteral | DoubleLiteral |
      whitespace();
      parse_StepExpr();
    }
    eventHandler.endNonterminal("RelativePathExpr", e0);
  }

  function try_RelativePathExpr()
  {
    try_StepExpr();
    for (;;)
    {
      switch (l1)
      {
      case 26:                      // '!'
        lookahead2W(266);           // Wildcard | EQName^Token | IntegerLiteral | DecimalLiteral | DoubleLiteral |
        break;
      default:
        lk = l1;
      }
      if (lk != 25                  // EOF
       && lk != 27                  // '!='
       && lk != 37                  // ')'
       && lk != 38                  // '*'
       && lk != 40                  // '+'
       && lk != 41                  // ','
       && lk != 42                  // '-'
       && lk != 46                  // '/'
       && lk != 47                  // '//'
       && lk != 49                  // ':'
       && lk != 53                  // ';'
       && lk != 54                  // '<'
       && lk != 57                  // '<<'
       && lk != 58                  // '<='
       && lk != 60                  // '='
       && lk != 61                  // '>'
       && lk != 62                  // '>='
       && lk != 63                  // '>>'
       && lk != 69                  // ']'
       && lk != 70                  // 'after'
       && lk != 75                  // 'and'
       && lk != 79                  // 'as'
       && lk != 80                  // 'ascending'
       && lk != 81                  // 'at'
       && lk != 84                  // 'before'
       && lk != 87                  // 'by'
       && lk != 88                  // 'case'
       && lk != 89                  // 'cast'
       && lk != 90                  // 'castable'
       && lk != 94                  // 'collation'
       && lk != 99                  // 'contains'
       && lk != 105                 // 'count'
       && lk != 109                 // 'default'
       && lk != 113                 // 'descending'
       && lk != 118                 // 'div'
       && lk != 122                 // 'else'
       && lk != 123                 // 'empty'
       && lk != 126                 // 'end'
       && lk != 128                 // 'eq'
       && lk != 131                 // 'except'
       && lk != 137                 // 'for'
       && lk != 146                 // 'ge'
       && lk != 148                 // 'group'
       && lk != 150                 // 'gt'
       && lk != 151                 // 'idiv'
       && lk != 160                 // 'instance'
       && lk != 162                 // 'intersect'
       && lk != 163                 // 'into'
       && lk != 164                 // 'is'
       && lk != 172                 // 'le'
       && lk != 174                 // 'let'
       && lk != 178                 // 'lt'
       && lk != 180                 // 'mod'
       && lk != 181                 // 'modify'
       && lk != 186                 // 'ne'
       && lk != 198                 // 'only'
       && lk != 200                 // 'or'
       && lk != 201                 // 'order'
       && lk != 205                 // 'paragraphs'
       && lk != 220                 // 'return'
       && lk != 224                 // 'satisfies'
       && lk != 232                 // 'sentences'
       && lk != 236                 // 'stable'
       && lk != 237                 // 'start'
       && lk != 247                 // 'times'
       && lk != 248                 // 'to'
       && lk != 249                 // 'treat'
       && lk != 254                 // 'union'
       && lk != 266                 // 'where'
       && lk != 270                 // 'with'
       && lk != 273                 // 'words'
       && lk != 279                 // '|'
       && lk != 280                 // '||'
       && lk != 281                 // '|}'
       && lk != 282                 // '}'
       && lk != 23578               // '!' '/'
       && lk != 24090)              // '!' '//'
      {
        lk = memoized(3, e0);
        if (lk == 0)
        {
          var b0A = b0; var e0A = e0; var l1A = l1;
          var b1A = b1; var e1A = e1; var l2A = l2;
          var b2A = b2; var e2A = e2;
          try
          {
            switch (l1)
            {
            case 46:                // '/'
              shiftT(46);           // '/'
              break;
            case 47:                // '//'
              shiftT(47);           // '//'
              break;
            default:
              shiftT(26);           // '!'
            }
            lookahead1W(265);       // Wildcard | EQName^Token | IntegerLiteral | DecimalLiteral | DoubleLiteral |
            try_StepExpr();
            memoize(3, e0A, -1);
            continue;
          }
          catch (p1A)
          {
            b0 = b0A; e0 = e0A; l1 = l1A; if (l1 == 0) {end = e0A;} else {
            b1 = b1A; e1 = e1A; l2 = l2A; if (l2 == 0) {end = e1A;} else {
            b2 = b2A; e2 = e2A; end = e2A; }}
            memoize(3, e0A, -2);
            break;
          }
        }
      }
      if (lk != -1
       && lk != 46                  // '/'
       && lk != 47)                 // '//'
      {
        break;
      }
      switch (l1)
      {
      case 46:                      // '/'
        shiftT(46);                 // '/'
        break;
      case 47:                      // '//'
        shiftT(47);                 // '//'
        break;
      default:
        shiftT(26);                 // '!'
      }
      lookahead1W(265);             // Wildcard | EQName^Token | IntegerLiteral | DecimalLiteral | DoubleLiteral |
      try_StepExpr();
    }
  }

  function parse_StepExpr()
  {
    eventHandler.startNonterminal("StepExpr", e0);
    switch (l1)
    {
    case 82:                        // 'attribute'
      lookahead2W(285);             // EQName^Token | S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' |
      break;
    case 121:                       // 'element'
      lookahead2W(283);             // EQName^Token | S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' |
      break;
    case 184:                       // 'namespace'
    case 216:                       // 'processing-instruction'
      lookahead2W(282);             // NCName^Token | S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' |
      break;
    case 96:                        // 'comment'
    case 119:                       // 'document'
    case 202:                       // 'ordered'
    case 244:                       // 'text'
    case 256:                       // 'unordered'
      lookahead2W(247);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
      break;
    case 78:                        // 'array'
    case 124:                       // 'empty-sequence'
    case 152:                       // 'if'
    case 165:                       // 'item'
    case 167:                       // 'json-item'
    case 242:                       // 'structured-item'
    case 243:                       // 'switch'
    case 253:                       // 'typeswitch'
      lookahead2W(240);             // S^WS | EOF | '!' | '!=' | '#' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' |
      break;
    case 73:                        // 'ancestor'
    case 74:                        // 'ancestor-or-self'
    case 93:                        // 'child'
    case 111:                       // 'descendant'
    case 112:                       // 'descendant-or-self'
    case 135:                       // 'following'
    case 136:                       // 'following-sibling'
    case 206:                       // 'parent'
    case 212:                       // 'preceding'
    case 213:                       // 'preceding-sibling'
    case 229:                       // 'self'
      lookahead2W(246);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
      break;
    case 6:                         // EQName^Token
    case 70:                        // 'after'
    case 72:                        // 'allowing'
    case 75:                        // 'and'
    case 77:                        // 'append'
    case 79:                        // 'as'
    case 80:                        // 'ascending'
    case 81:                        // 'at'
    case 83:                        // 'base-uri'
    case 84:                        // 'before'
    case 85:                        // 'boundary-space'
    case 86:                        // 'break'
    case 88:                        // 'case'
    case 89:                        // 'cast'
    case 90:                        // 'castable'
    case 91:                        // 'catch'
    case 94:                        // 'collation'
    case 97:                        // 'constraint'
    case 98:                        // 'construction'
    case 101:                       // 'context'
    case 102:                       // 'continue'
    case 103:                       // 'copy'
    case 104:                       // 'copy-namespaces'
    case 105:                       // 'count'
    case 106:                       // 'decimal-format'
    case 108:                       // 'declare'
    case 109:                       // 'default'
    case 110:                       // 'delete'
    case 113:                       // 'descending'
    case 118:                       // 'div'
    case 120:                       // 'document-node'
    case 122:                       // 'else'
    case 123:                       // 'empty'
    case 125:                       // 'encoding'
    case 126:                       // 'end'
    case 128:                       // 'eq'
    case 129:                       // 'every'
    case 131:                       // 'except'
    case 132:                       // 'exit'
    case 133:                       // 'external'
    case 134:                       // 'first'
    case 137:                       // 'for'
    case 141:                       // 'ft-option'
    case 145:                       // 'function'
    case 146:                       // 'ge'
    case 148:                       // 'group'
    case 150:                       // 'gt'
    case 151:                       // 'idiv'
    case 153:                       // 'import'
    case 154:                       // 'in'
    case 155:                       // 'index'
    case 159:                       // 'insert'
    case 160:                       // 'instance'
    case 161:                       // 'integrity'
    case 162:                       // 'intersect'
    case 163:                       // 'into'
    case 164:                       // 'is'
    case 166:                       // 'json'
    case 170:                       // 'last'
    case 171:                       // 'lax'
    case 172:                       // 'le'
    case 174:                       // 'let'
    case 176:                       // 'loop'
    case 178:                       // 'lt'
    case 180:                       // 'mod'
    case 181:                       // 'modify'
    case 182:                       // 'module'
    case 185:                       // 'namespace-node'
    case 186:                       // 'ne'
    case 191:                       // 'node'
    case 192:                       // 'nodes'
    case 194:                       // 'object'
    case 198:                       // 'only'
    case 199:                       // 'option'
    case 200:                       // 'or'
    case 201:                       // 'order'
    case 203:                       // 'ordering'
    case 218:                       // 'rename'
    case 219:                       // 'replace'
    case 220:                       // 'return'
    case 221:                   