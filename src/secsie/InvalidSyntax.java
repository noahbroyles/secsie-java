package secsie;

public class InvalidSyntax extends Exception {

	/**
	 * ParsingError serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	
	public InvalidSyntax(String line, String errorMessage, int lineNumber) {
		super(String.format("A parsing error occured on line %i: \"%s\" - %s", lineNumber, line, errorMessage));
	}
	
	public InvalidSyntax(String errorMessage, int lineNumber) {
		super(String.format("A parsing error occured on line %i: %s", lineNumber, errorMessage));
	}
	
	public InvalidSyntax(String errorMessage) {
		super(errorMessage);
	}
	
}
