grammar SipUrl;

sipUrl : phoneNumber? at? (ipv4 | domainName);

at : AT;
AT : '@';

NEWLINE : '\r'? '\n';

COLON : ':';

phoneNumber : PHONE_NUMBER;

PHONE_NUMBER : (DIGIT | '+' | '-' | '*')+;

DIGIT : [0-9];

BYTE : DIGIT DIGIT? DIGIT?;

PORT : DIGIT DIGIT? DIGIT? DIGIT? DIGIT?;

NOT_SEMICOLON : ~[;];

SEMICOLON : ';';

ANY : .;

domainName : DOMAIN_NAME SEMICOLON? ANY*? NEWLINE? EOF;

ALPHABETIC : [a-zA-Z] ;


DOMAIN_NAME : ALPHABETIC .*? '.' NOT_SEMICOLON+ ':'? PORT?;


ipv4 : IPV4 SEMICOLON? ANY*? NEWLINE? EOF;

IPV4 : BYTE '.' BYTE '.' BYTE '.' BYTE ':'? PORT?;

/* TODO add ipv6 support */