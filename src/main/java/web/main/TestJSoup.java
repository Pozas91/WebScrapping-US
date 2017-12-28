package web.main;

import web.scheduled.Batcher;

import java.io.IOException;

public class TestJSoup {

	public static void main(String... args) throws IOException {

        Batcher batcher = new Batcher();
        batcher.prepare();
    }
}