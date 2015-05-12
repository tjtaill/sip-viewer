grammar XsLog;


xsLog : xsLogItem+;
          
          
xsLogItem : dateTimeLine | directionLine | sipMessage | unknownLine;



sipMessage : sipResponse | sipRequest;

sipResponse : responseLine sipHeader+ sdp?;
 
RESPONSE_LINE : 'SIP/2.0' .*? RESPONSE_CODE .*? '\n';

responseLine : RESPONSE_LINE;

                 
RESPONSE_CODE : DIGIT DIGIT DIGIT;

METHOD : 'INVITE' | 
         'ACK' |
         'BYE' |
         'CANCEL' |
         'OPTIONS' |
         'REGISTER' |
         'PRACK' |
         'SUBSCRIBE' |
         'NOTIFY'|
         'PUBLISH' |
         'INFO' |
         'REFER' |
         'MESSAGE' |
         'UPDATE' ;

sipRequest : requestLine sipHeader+ sdp?;

requestLine : REQUEST_LINE;

REQUEST_LINE : METHOD ' '+ SIP_URL ' '+ SIP_VERSION NEWLINE; 


SIP_VERSION : 'SIP/2.0';
NOT_SPACE : ~' ';
SIP_URL : 'sip:' NOT_SPACE+;

sipHeader : via 
       | to
       | from
       | cseq
       | callId
       | allow
       | contact
       | maxForwards
       | subject
       | route 
       | supported
       | contentType
       | contentLength
       | require
       | rseq
       | rack 
       | session
       | privacy
       | pAssertedId
       | callInfo
       | accept
       | recordRoute
       | pServerUser
       | pAccessNetworkInfo
       | pChargingVector
       | pChargingFunctionAddresses
       | remotePartyId
       | userAgent
       | event
       | acceptLanguage
       | allowEvents
       | reason
       | server
       | expires
       ;

via : VIA;
VIA : 'Via:' .*? NEWLINE;
to  : TO;
TO : 'To:' .*? NEWLINE;
from : FROM;
FROM :'From:' .*? NEWLINE;
cseq : CSEQ;
CSEQ : 'CSeq:' .*? NEWLINE;
callId: CALL_ID;
CALL_ID : 'Call-ID:' .*? NEWLINE;
allow: ALLOW;
ALLOW : 'Allow:' .*? NEWLINE;
contact: CONTACT;
CONTACT : 'Contact:' .*? NEWLINE;
maxForwards : MAX_FORWARDS;
MAX_FORWARDS : 'Max-Forwards:' .*? NEWLINE;
subject: SUBJECT;
SUBJECT : 'Subject:' .*? NEWLINE;
route: ROUTE;
ROUTE : 'Route:' .*? NEWLINE;
supported: SUPPORTED;
SUPPORTED : 'Supported:' .*? NEWLINE;
contentType : CONTENT_TYPE;
CONTENT_TYPE : 'Content-Type:'.*? NEWLINE;
contentLength : CONTENT_LENGTH;
CONTENT_LENGTH : 'Content-Length:' .*? NEWLINE;
require : REQUIRE;
REQUIRE : 'Require:' .*? NEWLINE;
rseq : RSEQ;
RSEQ : 'RSeq:' .*? NEWLINE;
rack : RACK;
RACK : 'RAck:' .*? NEWLINE;
session : SESSION;
SESSION : 'Session:' .*? NEWLINE;
privacy : PRIVACY;
PRIVACY : 'Privacy:' .*? NEWLINE;
pAssertedId : P_ASSERTED_ID;
P_ASSERTED_ID : 'P-Asserted-Identity' .*? NEWLINE;
callInfo : CALL_INFO;
CALL_INFO : 'Call-Info:' .*? NEWLINE;
accept : ACCEPT;
ACCEPT : 'Accept:' .*? NEWLINE;
recordRoute : RECORD_ROUTE;
RECORD_ROUTE : 'Record-Route:' .*? NEWLINE;
P_SERVER_USER : 'P-Served-User:' .*? NEWLINE;
pServerUser : P_SERVER_USER;
P_ACCESS_NETWORK_INFO : 'P-Access-Network-Info:' .*? NEWLINE;
pAccessNetworkInfo : P_ACCESS_NETWORK_INFO;
P_CHARGING_VECTOR : 'P-Charging-Vector:' .*? NEWLINE;
pChargingVector : P_CHARGING_VECTOR;
P_CHARGING_FUNCTION_ADDRESSES : 'P-Charging-Function-Addresses:' .*? NEWLINE;
pChargingFunctionAddresses : P_CHARGING_FUNCTION_ADDRESSES;

REMOTE_PARTY_ID : 'Remote-Party-ID:' .*? NEWLINE;
remotePartyId : REMOTE_PARTY_ID;

USER_AGENT : 'User-Agent:' .*? NEWLINE;
userAgent : USER_AGENT;

EVENT : 'Event:' .*? NEWLINE;
event : EVENT;

ACCEPT_LANGUAGE : 'Accept-Language:' .*? NEWLINE;
acceptLanguage : ACCEPT_LANGUAGE;

ALLOW_EVENTS : 'Allow-Events:' .*? NEWLINE;
allowEvents : ALLOW_EVENTS;

REASON : 'Reason:' .*? NEWLINE;
reason : REASON;

SERVER : 'Server:' .*? NEWLINE;
server : SERVER;

EXPIRES : 'Expires:' .*? NEWLINE;
expires : EXPIRES;



sdp : sdpLine+;
sdpLine : SDP_LINE;
SDP_LINE : [a-z] '=' .*? NEWLINE;

WS : [ \t\r\n] -> skip;

DIGIT : [0-9];
YEAR : DIGIT DIGIT DIGIT DIGIT;
MONTH : DIGIT DIGIT;
DAY : DIGIT DIGIT;
DATE : YEAR '.' MONTH '.' DAY;

HOURS : DIGIT DIGIT;
MINUTES: DIGIT DIGIT;
SECONDS : DIGIT DIGIT;
FRACTION : DIGIT DIGIT DIGIT;
TIME : HOURS ':' MINUTES ':' SECONDS ':' FRACTION;
SPACE : ' ';
NEWLINE : '\r'? '\n';
DATE_TIME : DATE SPACE TIME;
DATE_TIME_LINE : DATE_TIME .*? NEWLINE; 
dateTimeLine : DATE_TIME_LINE;


DIRECTION : 'IN from'
          | 'OUT to'
          ;
IP : DIGIT+ '.' DIGIT+ '.' DIGIT+ '.' DIGIT+ ':'  DIGIT+;
DIRECTION_LINE : '\t' 'udp ' DIGIT+ ' Bytes ' DIRECTION SPACE IP NEWLINE;
directionLine : DIRECTION_LINE;


unknownLine : UNKNOWN_LINE;
UNKNOWN_LINE: .*? NEWLINE;
