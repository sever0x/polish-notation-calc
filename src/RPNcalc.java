import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class RPNcalc {
    // A place to store variables, if any
    public static HashMap<String, Double> variables = new HashMap<>();

    /**
     * The method takes initial input of strings, formats and iterates over the variables, if any.
     * The first parameter is ALWAYS formula (by condition)
     * The program will return a value only if at least some arguments are passed
     *
     * @param args entered formula and variables
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            args[0] = args[0].replaceAll("\\s+", "");
            System.out.println("Solve expression: " + args[0] + "\n");

            // We iterate over everything that is after the entered formula
            for (int i = 1; i < args.length; i++) {
                findVariables(args[i]);
            }
            ArrayList<String> expression = getTokens(args[0]);
            // The program will work only if the formula is entered correctly
            ArrayList<String> sort = reversePolishNotation(expression);
            calculate(sort);
        }
    }

    /**
     * The method parses a string with a formula, first breaking it into tokens,
     * then using the reverse Polish notation method
     *
     * @param parse the string passed as the first parameter
     */
    public static void calculate(ArrayList<String> parse) {
        System.out.println("\nResult: " + solve(parse));
        Scanner sc = new Scanner(System.in);

        // If variables were entered, the user can change their values
        if (!variables.isEmpty()) {
            System.out.println("\nDo you want to change the value of the variables? (y/n)");
            String agree = sc.next();

            if (agree.equalsIgnoreCase("y")) {
                reenterVariable();
                calculate(parse);
            }
        }
    }

    /**
     * The method looks for variables and writes them to the HashMap.
     * If necessary, return the error text to the user
     *
     * @param expression a string with the value of a variable
     */
    private static void findVariables(String expression) {
        // Variables that will store the final result
        char key = '0';
        String value = "";

        for (int i = 0; i < expression.length(); i++) {
            // If we find a letter (variable name)
            if (Character.isAlphabetic(expression.charAt(i))) {
                key = expression.charAt(i);
            }
            // If we find the number
            else if (Character.isDigit(expression.charAt(i)) || isUnaryOperator(expression.charAt(i))) {
                value += expression.charAt(i);
            }
            // If we find the separator of the fractional number
            else if (expression.charAt(i) == ',' || expression.charAt(i) == '.') {
                if (value.isEmpty()) {
                    value += "0";
                }
                value += ".";
            }
        }

        // If the variable contains a value, we will add it to the HashMap.
        // Otherwise, the program will notify the user that he entered the wrong value.
        if (value.length() > 0) {
            System.out.println(key + " = " + value);
            variables.put("" + key, Double.parseDouble(value));
        } else {
            System.out.println(typeError(2));
        }
    }

    /**
     * The method allows you to overwrite existing variables by replacing their values
      */
    private static void reenterVariable() {
        // For convenience, we get a list of variables
        String[] set = variables.keySet().toArray(new String[0]);
        Scanner sc = new Scanner(System.in);
        String value;

        for (int i = 0; i < set.length; i++) {
            System.out.print(set[i] + " = ");
            value = sc.nextLine();
            value = value.replaceAll(",", ".");

            variables.put(set[i], Double.parseDouble(value));
        }
    }

    /**
     * The method splits the string into tokens for further convenient work with them.
     * Allows for unary operators and fractional separators.
     *
     * @param expression formula to parse
     * @return either an array  with tokens, or null in case of erroneous input
     */
    private static ArrayList<String> getTokens(String expression) {
        // The results returned by the method
        ArrayList<String> result = new ArrayList<>();

        // Variable for intermediate results
        String value = "";
        // variable for collecting functions
        String func = "";

        boolean isDigit = false;
        boolean nextUnary = false;

        for (int i = 0; i < expression.length(); i++) {
            // For convenience, let's create a variable that will store the symbol
            char temp = expression.charAt(i);

            // If the character is a digit, or if it is a decimal separator
            if (Character.isDigit(temp) || temp == '.' || temp == ',') {
                // If we didn't have any features found
                if (func.equals("")) {
                    nextUnary = false;
                    // When we find a digit, we enable this checkbox to post-process the operator
                    isDigit = true;
                    // Replace the incorrect number separator
                    if (temp == ',') {
                        value += ".";
                    } else {
                        // If the user entered an incorrect fractional number
                        if (value.isEmpty() && temp == '.' || temp == ',') {
                            value += "0";
                        }
                        value += temp;
                    }

                    // If this is the last digit, then simply add to the array
                    if (i == expression.length() - 1) {
                        result.add(value);
                    }
                } else {
                    func += temp;
                }
            }
            // If we find a letter, then we write it into a variable of functions,
            // but we do not exclude that it can be a variable
            else if (Character.isAlphabetic(temp)) {
                nextUnary = false;
                func += temp;
                if (i == expression.length() - 1) {
                    result.add("" + temp);
                }
            }
            // If we found an operator
            else {
                // If before that we found numbers, then we add,
                // clear and turn off the checkbox that signals that there was a number before
                if (isDigit) {
                    result.add(value);

                    value = "";
                    isDigit = false;
                }
                // If before that we found an operator, and found it again, this operator will be unary
                if (nextUnary || i == 0 && expression.length() > 1) {
                    if (isUnaryOperator(temp) && value.length() == 0) {
                        value += temp;
                    } else if (temp == '(' || temp == ')') {
                        result.add("" + temp);
                    } else {
                        System.out.println(typeError(1));
                        System.exit(0);
                    }
                } else {
                    if (i == expression.length() - 1) {
                        // if the line contains some information, we need to understand what it is
                        if (!func.equals("")) {
                            // checking if there is such a variable
                            if (variables.containsKey(func)) {
                                result.add(func);
                            }
                            // checking if there is such a function
                            else if (isFunction(func)) {
                                result.add(func);
                            }
                            // the user was wrong
                            else {
                                System.out.println(typeError(3));
                                System.exit(0);
                            }
                        }
                        if (temp == ')') {
                            result.add("" + temp);
                        } else {
                            System.out.println(typeError(1));
                            System.exit(0);
                        }
                    } else {
                        // if the line contains some information, we need to understand what it is
                        if (!func.equals("")) {
                            if (variables.containsKey(func)) {
                                result.add(func);
                            } else if (isFunction(func)) {
                                result.add(func);
                            }
                            else {
                                System.out.println(typeError(3));
                                System.exit(0);
                            }
                            func = "";
                        }
                        result.add("" + temp);
                        // If we find an operator, turn on the checkbox that will
                        // help us understand what to do with the next character
                        nextUnary = temp != ')';

                    }
                }
            }
        }

        return result;
    }

    /**
     * The method processes the resulting formula using the reverse Polish notation method
     *
     * @param expression array with parsed formula
     * @return array with parsed expression
     */
    private static ArrayList<String> reversePolishNotation(ArrayList<String> expression) {
        ArrayList<String> output = new ArrayList<>();
        ArrayList<String> stack = new ArrayList<>();

        for (int i = 0; i < expression.size(); i++) {
            // Get the value of an array element
            String value = expression.get(i);
            // If it is an operand, then add it to the output array
            if (isOperand(value)) {
                output.add(value);
            } else if (variables.containsKey(value)) {
                output.add(value);
            }
            // Operand processing
            else if (isOperator(value) || isFunction(value)) {
                // We look if we have an empty stack or the operator is higher in priority than the previous one
                if (stack.size() == 0 || getPriority(value) > getPriority(stack.get(stack.size() - 1))) {
                    stack.add(value);
                }
                // If operands with the same priority
                else if (getPriority(value) == getPriority(stack.get(stack.size() - 1))) {
                    // Add the operator from the end of the stack to the output array
                    output.add(stack.get(stack.size() - 1));
                    stack.remove(stack.size() - 1);
                    stack.add(value);
                }
                // If the operator is lower in priority than the previous one
                else if (getPriority(value) <= getPriority(stack.get(stack.size() - 1))) {
                    // Reverse the stack and add it to the output array
                    if (!value.equals("(") && !value.equals(")")) {
                        Collections.reverse(stack);
                        output.addAll(stack);
                        stack.removeAll(stack);
                        stack.add(value);
                    } else if (value.equals("(")) {
                        stack.add(value);
                    } else {
                        // if you find a closing parenthesis
                        while (!stack.get(stack.size() - 1).equals("(")) {
                            output.add(stack.get(stack.size() - 1));
                            stack.remove(stack.size() - 1);
                        }
                        stack.remove(stack.size() - 1);
                        // if before that there was a function, we also add it to the output array
                        if (isFunction(stack.get(stack.size() - 1))) {
                            output.add(stack.get(stack.size() - 1));
                            stack.remove(stack.size() - 1);
                        }
                    }
                }
            }

        }
        // If we have reached the end and we still have operands on the stack
        if (stack.size() > 0) {
            Collections.reverse(stack);
            output.addAll(stack);
        }

        return output;
    }

    /**
     * Checks if a string is an operand
     *
     * @param operand value
     * @return true or false
     */
    private static boolean isOperand(String operand) {
        boolean checker = false;

        for (int i = 0; i < operand.length(); i++) {
            if (operand.charAt(i) == '.' || Character.isDigit(operand.charAt(i))) {
                checker = true;
            } else if (Character.isAlphabetic(operand.charAt(i))) {
                return false;
            }
        }

        return checker;
    }

    /**
     * Checks if a string is an operator
     *
     * @param operator value
     * @return true or false
     */
    private static boolean isOperator(String operator) {
        return operator.equals("*") || operator.equals("/") || operator.equals("+")
                || operator.equals("-") || operator.equals("^") || operator.equals("(") || operator.equals(")");
    }

    /**
     * Checks if a symbol is an unary operator
     *
     * @param operator symbol
     * @return true or false
     */
    private static boolean isUnaryOperator(char operator) {
        return operator == '+' || operator == '-';
    }

    /**
     * Checks if a string is a function
     * @param func function
     * @return true or false
     */
    private static boolean isFunction(String func) {
        return func.equals("sin") || func.equals("cos") || func.equals("tan") || func.equals("atan")
                || func.equals("log10") || func.equals("log2") || func.equals("sqrt");
    }

    /**
     * The method determines the priorities for operators
     *
     * @param operator symbol
     * @return numeric priority
     */
    private static int getPriority(String operator) {
        return switch (operator) {
            case "^", "sin", "cos", "tan", "atan", "log10", "log2", "sqrt" -> 3;
            case "*", "/" -> 2;
            case "+", "-" -> 1;
            case "(" -> -1;
            case ")" -> -2;
            default -> 0;
        };
    }

    /**
     * The method solves the entered expression
     *
     * @param expression parsed array
     * @return the null value of the array
     */
    private static double solve(ArrayList<String> expression) {
        ArrayList<Double> value = new ArrayList<>();

        for (int i = 0; i < expression.size(); i++) {
            String temp = expression.get(i);

            // If the resulting string is not an operator, then convert to double
            if (!isOperator(temp) && !isFunction(temp)) {
                if (variables.containsKey(temp)) {
                    value.add(variables.get(temp));
                }
                else if (temp.matches("[-]?[0-9]+[,.]?[0-9]*([\\/][0-9]+[,.]?[0-9]*)*")){
                    value.add(Double.parseDouble(temp));
                } else {
                    System.out.println(typeError(1));
                    System.exit(0);
                }
            }
            // If the resulting string is an operator
            else if (isOperator(temp)) {
                // If the array stores some values
                if (value.size() > 1) {
                    double newValue = defineOperator(temp, value.get(value.size() - 2), value.get(value.size() - 1));
                    // Replace old value with new one
                    value.set(value.size() - 2, newValue);
                    value.remove(value.size() - 1);
                }
            } else if (isFunction(temp)) {
                double newValue = defineFunc(temp, value.get(value.size() - 1));
                // Replace old value with new one
                value.set(value.size() - 1, newValue);
            }
        }

        return value.get(0);
    }

    /**
     * Performs a mathematical operation depending on the operator
     *
     * @param operator string
     * @param first    double
     * @param second   double
     * @return new double
     */
    private static double defineOperator(String operator, double first, double second) {
        return switch (operator) {
            case "+" -> first + second;
            case "-" -> first - second;
            case "*" -> first * second;
            case "/" -> first / second;
            case "^" -> Math.pow(first, second);
            default -> 0.0;
        };
    }

    /**
     * Performs computation depending on the function
     * @param func string
     * @param value double
     * @return new double
     */
    private static double defineFunc(String func, double value) {
        return switch (func) {
            case "sin" -> Math.sin(value);
            case "cos" -> Math.cos(value);
            case "tan" -> Math.tan(value);
            case "atan" -> Math.atan(value);
            case "log10" -> Math.log10(value);
            case "log2" -> Math.log(value) / Math.log(2);
            case "sqrt" -> Math.sqrt(value);
            default -> 0.0;
        };
    }

    /**
     * Returns a message to the user that describes an input error from the user
     *
     * @param code error number
     * @return error message
     */
    private static String typeError(int code) {
        return switch (code) {
            case 1 -> "Incorrect formula entry. Please check your input.";
            case 2 -> "Incorrect variable entry.";
            case 3 -> "The variable or function does not exist. Please check your input.";
            default -> "Unknown error...";
        };
    }
}
