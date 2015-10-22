package com.example.grzegorz.androidterminalemulator.netstat;

import java.io.*;
import java.util.*;


//todo: name
public class NetstatUtils {

    //todo: mapa connectionType na plik

    //todo: name
    private static final Map connectionTypeToFile;

    static {
        connectionTypeToFile = new HashMap<ConnectionType, String[]>();
        connectionTypeToFile.put(ConnectionType.TCP, new String[]{"/proc/net/tcp", "/proc/net/tcp6"});
        connectionTypeToFile.put(ConnectionType.UDP, new String[]{"/proc/net/udp", "/proc/net/udp6"});
    }




    private static int getInt16(final String hexAddress) {
        try {
            return Integer.parseInt(hexAddress, 16);
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getRouting() {
        //todo: routing dla ipv6

        StringBuilder stringBuilder = new StringBuilder();

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader("/proc/net/route"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        int i = 0;
        stringBuilder.append("Iface\tDestination\tGateway\tFlags\tMetric\tMask\tMTU\tWINDOW\tIRRT\n");
        try {
            while((line= in.readLine()) != null) {
                if (i++ != 0) { //first line with column names
                    line = line.trim();
                    String[] fields = line.split("\\s+", 11); //todo: check number of fields //todo: move splitting to seperate function
                    RoutingV4 routing = new RoutingV4(fields[0],fields[1],fields[2],fields[3],fields[6],
                            fields[7],fields[8],fields[9],fields[10]); //todo: refactor?
                    stringBuilder.append(routing.toConsoleString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    //todo: merge routing v4 and v6
    public static void getRoutingV6() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader("/proc/net/ipv6_route"));
        String line;
        int i = 0;
        System.out.println("Iface\tDestination\tGateway\tFlags\tMetric\tMask\tMTU\tWINDOW\tIRRT\n");
        while((line= in.readLine()) != null) {
                line = line.trim();
                String[] fields = line.split("\\s+", 10); //todo: check number of fields //todo: move splitting to seperate function
                RoutingV6 routing = new RoutingV6(fields[0],fields[1],fields[2],fields[3],fields[4],
                        fields[5],fields[6],fields[7],fields[8],fields[9]); //todo: refactor?
                System.out.println(routing.toConsoleString());
        }
    }


    public static String getConnections(ConnectionType connectionType) {
        List<Association> connections = new ArrayList<Association>();

        StringBuilder stringBuilder = new StringBuilder();

        try {
            for (String fileName : (String[]) connectionTypeToFile.get(connectionType)) {

                BufferedReader in = new BufferedReader(new FileReader(fileName));
                String line;
                int i = 0;
                while ((line = in.readLine()) != null) {
                    line = line.trim(); //todo: why?
                    String[] fields = line.split("\\s+", 10); //why 10?

                    if (!fields[0].equals("sl")) {

                        String src[] = fields[1].split(":", 2);
                        String dst[] = fields[2].split(":", 2);

                        Association association = new Association(IpAddress.getAddress(src[0]), String.valueOf(getInt16(src[1])),
                                IpAddress.getAddress(dst[0]), String.valueOf(getInt16(dst[1])), fields[7], connectionType,
                                connectionType == ConnectionType.TCP ? TCP.TcpState.getById(Integer.parseInt(fields[3], 16)) : null);
                        stringBuilder.append(association.toString());
                        i++;
                    }
                }
            }
        } catch (Exception e) {

        }

        return stringBuilder.toString();


    }

    public static void main(String[] args) throws IOException {

        //talibca routingu netstat -r
        //add listen method?
//        getConnections(ConnectionType.TCP);
//        getConnections(ConnectionType.UDP);

//        getRouting();
        getRoutingV6();

    }

}