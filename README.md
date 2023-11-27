# Expression evaluator documentation

This application was made as a job hiring assignment.

Application can be run locally by starting annotated main class, 
starting created application configuration or starting application by typing
**mvn spring-boot:run** in terminal or command prompt. 

Database used for development and testing purposes is H2 in-memory database 
which is configured to save records in memory and after server restarts
the records are erased.

Database can be accessed through web browser of choice at url 
http://localhost:8080/h2-console. 
Database access data is: JDBC URL: jdbc:h2:mem:testDatabase 

User Name: root Password: root

Application can be tested with Postman or other similar JSON compatible clients.

Application can be also tested with swagger at url: http://localhost:8080/api/v1/docs/swagger. 

**APPLICATION TESTING ROUTES**

Application has two routes that can be consumed.

1. http://localhost:8080/api/v1/logical-evaluator/expression
This route is a POST request taking in two values:
   1. expressionName -> Name of the expression
   2. expressionValue -> Expression to be evaluated later
This route returns either a newly saved expression UUID or an error response.

2. http://localhost:8080/api/v1/logical-evaluator/evaluate
This route is a POST request taking in three possible values:
   1. expressionId -> Must be exactly 36 characters and represent UUID key that is
   returned after /expression call
   2. expressionEvaluationJsonBody -> This field is optional and doesn't have to be
   included. It can be provided in pure JSON object form as for example:
      {
      "customer":
      {
      "firstName": "JOHN",
      "lastName": "DOE",
      "address":
      {
      "city": "Chicago",
      "zipCode": 1234,
      "street": "56th",
      "houseNumber": 2345
      },
      "salary": 99,
      "type": "BUSINESS"
      }
      }
   or as a STRING version of the JSON object as for example:
   "{\"customer\":{\"firstName\":\"JOHN\",\"lastName\":\"DOE\",\"address\":{\"city\":\"Chicago\",\"zipCode\":1234,\"street\":\"56th\",\"houseNumber\":2345},\"salary\":99,\"type\":\"BUSINESS\"}}"
   This field should be included if you have object value references inside your expression.
   For example if you have a notation of customer.firstName inside your expression if you want to evaluate this literal
   against some concrete value you will need to provide a proper JSON object with correct structure to parse this customer.firstName notation and to replace it with a real value from a JSON object.
   If you have notation like customer.firstName in your expression, and you don't provide a json object it will be evaluated a string literal if it can be. If it is being compared with relational operators
   like < or > the application will throw an error.
   If the json is in improper format form or missing any fields application will also throw and error.
   3. expressionLiteralValuePlaceholderList -> This field is also optional and doesn't have to be included.
   If your expression contains some string literal that wants to be replaced with another value you can add items
   in a list of these items. For each entry you need to provide "literalPlaceholderName" which is name of the literal to be replaced
   and "literalPlaceholderValue" which is the value that will replace each of the literalPlaceholderNames.
   If you don't provide these, and you have some literals in your expression they would be evaluated as string literals if it is possible.
   If they are compared using relational literals such as < or > the application will throw an error.

Example of a complex expression which uses JSON object data and string replacement literals:
!((customer.firstName == 'JOHN' && customer.salary < 100) OR TRUE AND (customer.test != null && CITY == 'Washington'))
This expression contains multiple references to customer object that will be provided from JSON object and CITY literal that
would be replaced with a concrete value you provide. 
Operators and operands should be divided by space in between them with exceptions of the NOT(!) operator than can be next to the operand
and the parentheses that can be also positioned right next to the NOT(!) operator and the operands.
Logical operators are supported in both AND, OR, NOT (ignoring case) as well as in &&, ||, ! form.
Boolean operands TRUE and FALSE are supported also ignoring case.
Relational operators <, >, <=, >=, == and != are supported.
Expression also supports parentheses for changing order of precedence inside expression.
Expression supports string literals as operands for comparison, number literals, integer or decimal as operands for comparison as well
as object values in form of {objectBody} that can be compared against object referenced from provided JSON value.
Null values are also supported and values can be compared against them.
