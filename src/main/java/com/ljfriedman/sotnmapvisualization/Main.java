package com.ljfriedman.sotnmapvisualization;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main extends Application {

    private static final int SCENEWIDTH = 1000;
    private static final int SCENEHEIGHT = 800;
    private static final double ROOM_SIZE = 5.5;
    private static final int FACE_SIZE = 16;

    private static final double CENTER_X = 167.5;
    private static final double CENTER_Z = 0;
    private static final double CAMERA_R = 500.0;
    private static final double CAMERA_I = 0.05;

    private Group map;
    private Set<Position> positions;
    private PerspectiveCamera camera;
    private double cameraAngle = Math.toRadians(180);

    @Override
    public void start(Stage primaryStage) throws Exception {

        map = new Group();
        positions = new HashSet<>();

        buildMap();

        Scene scene = new Scene(map, SCENEWIDTH, SCENEHEIGHT, true); // "true" enables 3D
        scene.setFill(Color.LIGHTBLUE); // bg color

        // event handler
        scene.setOnKeyPressed(this::handleKeyPress);

        // camera
        camera = new PerspectiveCamera(true);
        camera.setTranslateY(125);
        camera.setRotationAxis(Rotate.Y_AXIS);
        camera.setNearClip(50);
        camera.setFarClip(1000);

        scene.setCamera(camera);
        updateCameraPosition();

        primaryStage.setScene(scene);
        primaryStage.setTitle("Test 1");
        primaryStage.show();
    }

    private void buildMap() throws IOException {
        List<int[]> mapData = readRoomsData();

        for (int[] room : mapData) {
            addPosition(room);
        }

        for (int[] room : mapData) {
            createRoom(room);
        }
    }

    private List<int[]> readRoomsData() throws IOException {
        List<int[]> mapData = new ArrayList<>();
        InputStream inputStream = getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/rooms.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && line.startsWith("(") && line.endsWith(")")) {
                String[] values = line.substring(1, line.length() - 1).split(",\\s*");
                int[] roomData = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    roomData[i] = Integer.parseInt(values[i]);
                }
                mapData.add(roomData);
            }
        }

        reader.close();
        return mapData;
    }

    private void addPosition(int[] roomData) {
        int x = roomData[0];
        int y = roomData[1];
        int z = roomData[2];
        positions.add(new Position(x, y, z));
    }

    private void createRoom(int[] roomData) {
        int x = roomData[0];
        int y = roomData[1];
        int z = roomData[2];

        int right = roomData[3];
        int up = roomData[4];
        int left = roomData[5];
        int down = roomData[6];
        int front = roomData[7];
        int back = roomData[8];

        double posX = x * ROOM_SIZE;
        double posY = y * ROOM_SIZE;
        double posZ = z * ROOM_SIZE;

        MeshView roomMesh = customBox(ROOM_SIZE, right, up, left, down, front, back);
        roomMesh.setTranslateX(posX);
        roomMesh.setTranslateY(posY);
        roomMesh.setTranslateZ(posZ);

        map.getChildren().add(roomMesh);
    }

    private MeshView customBox(double size, int right, int up, int left, int down, int front, int back) {
        TriangleMesh mesh = new TriangleMesh();

        // Define points of the cube
        float halfSize = (float) size / 2;
        float[] points = {
                // front face
                -halfSize, -halfSize, -halfSize, // 0
                halfSize, -halfSize, -halfSize, // 1
                halfSize, halfSize, -halfSize, // 2
                -halfSize, halfSize, -halfSize, // 3
                // back face
                -halfSize, -halfSize, halfSize, // 4
                halfSize, -halfSize, halfSize, // 5
                halfSize, halfSize, halfSize, // 6
                -halfSize, halfSize, halfSize  // 7
        };
        mesh.getPoints().addAll(points);

        // Define texture coordinates
        float[] texCoords = {
                0.25f, 0f, // 0
                0.5f, 0f, // 1
                0f, 0.33f, // 2
                0.25f, 0.33f, // 3
                0.5f, 0.33f, // 4
                0.75f, 0.33f, // 5
                1f, 0.33f, // 6
                0f, 0.66f, // 7
                0.25f, 0.66f, // 8
                0.5f, 0.66f, // 9
                0.75f, 0.66f, // 10
                1f, 0.66f, // 11
                0.25f, 1f, // 12
                0.5f, 1f // 13
        };
        mesh.getTexCoords().addAll(texCoords);

        // Define faces (each face consists of two triangles)
        int[] faces = {

                // front face
                0, 3, 1, 4, 2, 9,   0, 3, 2, 9, 3, 8,
                // right face
                1, 4, 5, 5, 6, 10,  1, 4, 6, 10, 2, 9,
                // back face
                5, 5, 4, 0, 7, 7,   5, 5, 7, 7, 6, 10,
                // left face
                4, 0, 0, 3, 3, 8,   4, 0, 3, 8, 7, 7,
                // top face
                3, 8, 2, 9, 6, 10,  3, 8, 6, 10, 7, 7,
                // bottom face
                4, 0, 5, 5, 1, 4,   4, 0, 1, 4, 0, 3

        };
        mesh.getFaces().addAll(faces);

        WritableImage textureImage = new WritableImage(FACE_SIZE * 3, FACE_SIZE * 4);
        Canvas canvas = new Canvas(FACE_SIZE * 3, FACE_SIZE * 4);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Map faces to their correct positions in the combined texture
        // drawing the top of the cube
        if (up == 2 && right == 2 && left == 2) {
            gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/2222.png")), FACE_SIZE, 0, FACE_SIZE, FACE_SIZE);  // Top
        } else if (up == 2 && right == 2) {
            gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/2202.png")), FACE_SIZE, 0, FACE_SIZE, FACE_SIZE);
        } else if (up == 2 && left == 2) {
            gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/0222.png")), FACE_SIZE, 0, FACE_SIZE, FACE_SIZE);
        } else if (up == 2) {
            gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/0202.png")), FACE_SIZE, 0, FACE_SIZE, FACE_SIZE);
        } else {
            gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/0000.png")), FACE_SIZE, 0, FACE_SIZE, FACE_SIZE);
        }

        //drawing the front of the cube
        String frontName = String.valueOf(right) + String.valueOf(up) + String.valueOf(left) + String.valueOf(down);
        gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/" + frontName + ".png")), FACE_SIZE, FACE_SIZE, FACE_SIZE, FACE_SIZE);  // Front

        //drawing the bottom of the cube
        if (down == 2) {
            gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/2222.png")), FACE_SIZE, FACE_SIZE * 2, FACE_SIZE, FACE_SIZE);
        } else {
            gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/0000.png")), FACE_SIZE, FACE_SIZE * 2, FACE_SIZE, FACE_SIZE);
        }

        //drawing the back of the cube (flipped from front)
        String backName = String.valueOf(left) + String.valueOf(up) + String.valueOf(right) + String.valueOf(down);
        gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/" + backName + ".png")), FACE_SIZE, FACE_SIZE * 3, FACE_SIZE, FACE_SIZE);  // Back

        //drawing from the left of the cube
        if (left == 2) {
            gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/2222.png")), 0, FACE_SIZE, FACE_SIZE, FACE_SIZE);
        } else if (left == 1) {
            gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/1222.png")), 0, FACE_SIZE, FACE_SIZE, FACE_SIZE);
        } else {
            gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/0222.png")), 0, FACE_SIZE, FACE_SIZE, FACE_SIZE);
        }

        //drawing from the right of the cube
        if (right == 2) {
            gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/2222.png")), FACE_SIZE * 2, FACE_SIZE, FACE_SIZE, FACE_SIZE);
        } else if (right == 1) {
            gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/2212.png")), FACE_SIZE * 2, FACE_SIZE, FACE_SIZE, FACE_SIZE);
        } else {
            gc.drawImage(new Image(getClass().getResourceAsStream("/com/ljfriedman/sotnmapvisualization/SOTN-rooms/2202.png")), FACE_SIZE * 2, FACE_SIZE, FACE_SIZE, FACE_SIZE);
        }

        canvas.snapshot(null, textureImage);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(textureImage);

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);

        return meshView;
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.LEFT) {
            if (event.isShiftDown()) {
                cameraAngle = (double) Math.round((cameraAngle + Math.toRadians(CAMERA_I * 200)) * 100) / 100;
            } else {
                cameraAngle = (double) Math.round((cameraAngle + CAMERA_I) * 100) / 100;
            }
        } else if (event.getCode() == KeyCode.RIGHT) {
            if (event.isShiftDown()) {
                cameraAngle = (double) Math.round((cameraAngle - Math.toRadians(CAMERA_I * 200)) * 100) / 100;
            } else {
                cameraAngle = (double) Math.round((cameraAngle - CAMERA_I) * 100) / 100;
            }
        } else if (event.getCode() == KeyCode.DOWN) {
            double angleInDegrees = Math.toDegrees(cameraAngle);
            double nearest45Degrees = Math.round(angleInDegrees / 45.0) * 45.0;
            cameraAngle = Math.toRadians(nearest45Degrees);
        }
        updateCameraPosition();
    }

    private void updateCameraPosition() {
        double x = CENTER_X + CAMERA_R * Math.sin(cameraAngle);
        double z = CENTER_Z + CAMERA_R * Math.cos(cameraAngle);
        camera.setTranslateX(x);
        camera.setTranslateZ(z);

        double angle = (double) Math.round((Math.toDegrees(cameraAngle) + 180) * 100) / 100; // +180 to make the camera face towards the center
        camera.getTransforms().clear();
        camera.getTransforms().add(new Rotate(angle, Rotate.Y_AXIS));
    }

    public static void main(String[] args) { launch(args); }
}