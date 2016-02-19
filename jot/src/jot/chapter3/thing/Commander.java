package jot.chapter3.thing;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Commander {

    private static AtomicReference<Commander> instance
            = new AtomicReference<>();

    public static Commander getInstance() {
        if (instance.get() == null) {
            instance.set(new Commander());
        }

        return instance.get();
    }

    public interface Command {

        public String execute(String[] command);

        public String getHelp();
    }

    private Map<String, Command> commands = new HashMap<>();

    private Commander() {
        commands.put("list", new Command() {
            @Override
            public String execute(String[] command) {
                StringBuilder sb = new StringBuilder();
                Collection<Point> points
                        = PointHandler.getInstance().getPoints();
                for (Point point : points) {
                    sb.append(point.toString())
                            .append(System.lineSeparator());
                }

                return sb.toString();
            }

            @Override
            public String getHelp() {
                return "list: display list for point";
            }
        });

        commands.put("set", new Command() {
            @Override
            public String execute(String[] command) {
                if (command.length != 3) {
                    return "Invalid set command";
                } else {
                    int pointId = Integer.parseInt(command[1]);
                    Point point
                            = PointHandler.getInstance().getPoint(pointId);
                    if (point == null) {
                        return "Cannot find a point(" + pointId + ")";
                    } else if (point instanceof OutputPoint) {
                        int value = Integer.parseInt(command[2]);
                        OutputPoint writablePoint
                                = (OutputPoint) point;
                        writablePoint.setPresentValue(value);
                        return null;
                    } else {
                        return "It is not a output point(" + pointId + ")";
                    }
                }
            }

            @Override
            public String getHelp() {
                return "set: set the present value of point. format"
                        + "-> set [point id] [value]";
            }
        });

        commands.put("get", new Command() {
            @Override
            public String execute(String[] command) {
                if (command.length != 2) {
                    return "Invalid get command";
                } else {
                    int pointId = Integer.parseInt(command[1]);
                    Point point
                            = PointHandler.getInstance().getPoint(pointId);
                    if (point == null) {
                        return "Cannot find a point(" + pointId + ")";
                    } else {
                        return String.valueOf(point.getPresentValue());
                    }
                }
            }

            @Override
            public String getHelp() {
                return "get: get the present value of point. format"
                        + "-> get [point id]";
            }
        });
        
        commands.put("rename", new Command() {
            @Override
            public String execute(String[] command) {
                if (command.length != 3) {
                    return "Invalid rename command";
                } else {
                    int pointId = Integer.parseInt(command[1]);
                    Point point
                            = PointHandler.getInstance().getPoint(pointId);
                    if (point == null) {
                        return "Cannot find a point(" + pointId + ")";
                    } else {
                        point.setName(command[2]);
                        return null;
                    }
                }
            }

            @Override
            public String getHelp() {
                return "rename: change the name of point. format"
                        + "-> rename [point id] [new name]";
            }
        });
    }

    public String execute(String[] command) throws IOException {
        if (command.length == 0) {
            return null;
        }
        if (command[0].equals("help")) {
            return help();
        }

        Command cmd = commands.get(command[0]);
        if (cmd == null) {
            return "Invalid command: " + command[0];
        }

        return cmd.execute(command);
    }

    private String help() {
        StringBuilder sb = new StringBuilder();
        sb.append("Thing's commands")
                .append(System.lineSeparator());
        for (Command command : commands.values()) {
            sb.append(command.getHelp())
                    .append(System.lineSeparator());
        }

        return sb.toString();
    }

    public void register(String name, Command cmd) {
        commands.put(name, cmd);
    }

    public void unregister(String name) {
        commands.remove(name);
    }
}
