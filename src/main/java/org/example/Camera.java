package org.example;

import java.awt.geom.Point2D;

public class Camera {
    public double x = 0;
    public double y = 0;
    public double zoom = 1.0;

    private double minZoom = 0.2;
    private double maxZoom = 3.0;
    private double zoomSpeed = 0.1;

    private int viewWidth;
    private int viewHeight;

    public Camera(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        centerOn(viewWidth / 2.0, viewHeight / 2.0);
    }

    public void setViewSize(int width, int height) {
        this.viewWidth = width;
        this.viewHeight = height;
    }

    public void centerOn(double worldX, double worldY) {
        this.x = worldX;
        this.y = worldY;
    }

    public void zoomIn(double mouseX, double mouseY) {
        Point2D.Double worldPosBefore = screenToWorld(mouseX, mouseY);
        zoom = Math.min(zoom + zoomSpeed, maxZoom);
        Point2D.Double worldPosAfter = screenToWorld(mouseX, mouseY);

        // Justera kameran så att punkten under musen förblir på samma plats
        x += worldPosBefore.x - worldPosAfter.x;
        y += worldPosBefore.y - worldPosAfter.y;
    }

    public void zoomOut(double mouseX, double mouseY) {
        Point2D.Double worldPosBefore = screenToWorld(mouseX, mouseY);
        zoom = Math.max(zoom - zoomSpeed, minZoom);
        Point2D.Double worldPosAfter = screenToWorld(mouseX, mouseY);

        x += worldPosBefore.x - worldPosAfter.x;
        y += worldPosBefore.y - worldPosAfter.y;
    }

    public double worldToScreenX(double worldX) {
        return (worldX - x) * zoom + viewWidth / 2.0;
    }

    public double worldToScreenY(double worldY) {
        return (worldY - y) * zoom + viewHeight / 2.0;
    }

    public Point2D.Double screenToWorld(double screenX, double screenY) {
        double worldX = (screenX - viewWidth / 2.0) / zoom + x;
        double worldY = (screenY - viewHeight / 2.0) / zoom + y;
        return new Point2D.Double(worldX, worldY);
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = Math.max(minZoom, Math.min(maxZoom, zoom));
    }

    public void setZoomLimits(double min, double max) {
        this.minZoom = min;
        this.maxZoom = max;
        this.zoom = Math.max(minZoom, Math.min(maxZoom, zoom));
    }

    public void setZoomSpeed(double speed) {
        this.zoomSpeed = speed;
    }

    // För att få världens storlek baserat på zoom
    public double getWorldWidth() {
        return viewWidth / zoom;
    }

    public double getWorldHeight() {
        return viewHeight / zoom;
    }
}