import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GuessTest {
	
	Client clientConnection;
	int portVal = 5555;
	
	@BeforeEach
	void setup() {
		clientConnection = new Client();
	}
	
	
	@Test
	void init() {
		assertEquals("Client", clientConnection.getClass().getName(), "Did not inititialize server");
	}
	
	
	
	@Test
	void checkPortVal() {
		assertEquals(portVal, clientConnection.getPort(), "Port number not correct");
	}

	@Test
	void checkSetPort() {
		clientConnection.setPort(5556);
		assertEquals(5556, clientConnection.getPort(), "setPort wrong");
	}
	
	@Test
	void checkIP() {
		assertEquals("127.1.1.1", clientConnection.getIP(), "IP address not correct");
	}
	
	@Test
	void checkSetIP() {
		clientConnection.setIP("8.8.8.8");
		assertEquals("8.8.8.8", clientConnection.getIP(), "setIP wrong");
	}
	
	@Test
	void checkLengthOfWord() {
		assertEquals(10, clientConnection.lengthOfWord, "Length of word wrong");
	}
	
	@Test
	void checkCurrentGuess() {
		assertEquals('a', clientConnection.currentGuess, "Current guess wrong");
	}
	

}