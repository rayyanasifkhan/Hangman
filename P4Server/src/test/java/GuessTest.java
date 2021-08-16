import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javafx.application.Platform;

class GuessTest {
	
	private Server serverConnection;
	private WordGuessInfo info;
	private int portVal = 5555;
	
	@BeforeEach
	void setup() {
		portVal = portVal + 1;
		serverConnection = new Server(portVal);
	}
	
	
	@Test
	void init() {
		assertEquals("Server", serverConnection.getClass().getName(), "Did not inititialize server");
	}
	
	@Test
	void checkPortVal() {
		assertEquals(portVal, serverConnection.port, "Port number not correct");
	}

	@Test
	void checkWordGuessInfo() {
		assertEquals(1, serverConnection.count, "Client count wrong");
	}
	
}