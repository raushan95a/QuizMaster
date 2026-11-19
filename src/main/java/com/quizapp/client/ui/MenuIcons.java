package com.quizapp.client.ui;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

/** Small round icons for menu items, drawn on a Canvas and turned into an Image. */
final class MenuIcons {

    private MenuIcons() {
    }

    /** An ImageView with a coloured circle and a white letter, e.g. icon("?", Color.web("#2563eb")). */
    static ImageView icon(String letter, Color color) {
        Canvas canvas = new Canvas(16, 16);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(color);
        g.fillOval(0, 0, 16, 16);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("System", FontWeight.BOLD, 11));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.fillText(letter, 8, 8.5);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image image = canvas.snapshot(params, null);
        return new ImageView(image);
    }
}
