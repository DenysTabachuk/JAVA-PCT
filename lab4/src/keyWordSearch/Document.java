package keyWordSearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

class Document {
    private final String name;
    private final List<String> lines;

    public Document(String name, List<String> lines) {
        this.name = name;
        this.lines = lines;
    }

    public List<String> getLines() {
        return this.lines;
    }

    public String getName(){
        return name;
    }

    static Document fromFile(File file) throws IOException {
        List<String> lines = new LinkedList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        }
        return new Document(file.getName(), lines);
    }
}