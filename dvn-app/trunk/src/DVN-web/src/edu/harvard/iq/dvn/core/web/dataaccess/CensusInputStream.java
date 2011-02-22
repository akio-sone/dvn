package edu.harvard.iq.dvn.core.web.dataaccess;

// java core imports:
import java.io.IOException;
import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;



public class CensusInputStream extends FilterInputStream {
    public CensusInputStream (InputStream in) {
        super(in);

        this.rd = new BufferedReader(new InputStreamReader(in));
    }

    private BufferedReader rd = null;
    private Boolean initialized = false;
    private byte[] cachedData;
    private String[] requestedVariables = null;
    private int[] positions = null;

    @Override
    public int read (byte[] buffer) throws IOException {
        // it ain't pretty, yet;
        // still testing the concept, experimenting with performance, etc.

        String line;
        String[] valueTokens;
        int byteLen = 0;

        if (!initialized) {

            line = rd.readLine();
            if (line == null) {
                throw new IOException ("Could not read input from Census download servlet.");
            }

            // chop the newlines:
           line = line.replaceFirst("[\r\n]*$", "");

            // split on TABs:
            valueTokens = line.split("\t", -2);

            // determine the location of the variables that we want:
            for (int i = 0; i < requestedVariables.length; i++) {
                int j = 0;
                int position = -1;

                while (j < valueTokens.length && position < 0) {
                    if (requestedVariables[i].equals(valueTokens[j])) {
                        position = j;
                    }
                }

                if (position > -1) {
                    this.positions[i] = position;
                } else {
                    throw new IOException ("Requested variable "+requestedVariables[i]+
                            " not found in the Census data stream.");
                }
            }

            initialized = true;
        }

        if (cachedData != null && cachedData.length > 0) {
            if (cachedData.length < buffer.length) {
                for (int i = 0; i < cachedData.length; i++) {
                    buffer[i] = cachedData[i];
                }
                cachedData = null;
                byteLen = cachedData.length;
            } else {
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = cachedData[i];
                }
                byte[] leftoverCache = new byte[cachedData.length - buffer.length];
                for (int i = 0; i < cachedData.length - buffer.length; i++) {
                    leftoverCache[i] = cachedData[buffer.length + i];
                }
                cachedData = leftoverCache;
                return buffer.length;
            }
        }

        while (byteLen < buffer.length) {
            line = rd.readLine();
            if (line == null) {
                return byteLen;
            }

            line = line.replaceFirst("[\r\n]*$", "");
            valueTokens = line.split("\t", -2);

            int byteLenLocal = 0;

            for (int i = 0; i < positions.length; i++) {
                byteLenLocal += valueTokens[positions[i]].length();
            }

            byte[] dataSelected = new byte[byteLenLocal];

            int k = 0;

            for (int i = 0; i < positions.length; i++) {
                byte[] valueTokenBytes = valueTokens[positions[i]].getBytes();
                for (int j = 0; j < valueTokenBytes.length; j++) {
                    dataSelected[k++] = valueTokenBytes[j];
                }
            }

            if (k != byteLenLocal) {
                throw new IOException ("error parsing Census byte stream");
            }

            if (byteLen + byteLenLocal < buffer.length) {
                for (int i = 0; i < byteLenLocal; i++) {
                    buffer[byteLen+i] = dataSelected[i];
                }
                byteLen += byteLenLocal;
            } else {
                for (int i = 0; i<buffer.length-byteLen; i++) {
                    buffer[byteLen+i] = dataSelected[i];
                }

                cachedData = new byte[byteLenLocal - buffer.length + byteLen];
                for (int i = 0; i < cachedData.length - buffer.length; i++) {
                    cachedData[i] = dataSelected[i + buffer.length - byteLen];
                }
                return buffer.length;
            }
        }

        return byteLen;
    }


    @Override
    public int read () throws IOException {
        if (true) {
            throw new IOException ("Census input stream: read() not supported.");
        }
        return 0;
    }

    @Override
    public int read (byte[] buffer, int a, int b) throws IOException {
        if (true) {
            throw new IOException ("Census input stream: read() not supported.");
        }

        return 0;
    }
    
    public void setRequestedVariables (String[] requestedVariables) {
        this.requestedVariables = requestedVariables;
        this.positions = new int[requestedVariables.length];
    }
}