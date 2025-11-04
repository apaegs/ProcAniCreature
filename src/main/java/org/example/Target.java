package org.example;

import java.awt.geom.Point2D;

public class Target {
    public Point2D.Double position;
    public double vx, vy;
    public double speed;

    public Target(double startX, double startY, double vx, double vy, double speed) {
        position = new Point2D.Double(startX, startY);
        this.vx = vx;
        this.vy = vy;
        this.speed = speed;
    }

    public void update(double deltaTime, int width, int height) {
        int margin = 20;
        position.x += vx * deltaTime;
        position.y += vy * deltaTime;

        // Studsa från kanterna med bättre hantering
        boolean bounced = false;
        if (position.x < margin) {
            vx = Math.abs(vx);
            position.x = margin;
            bounced = true;
        } else if (position.x > width - margin) {
            vx = -Math.abs(vx);
            position.x = width - margin;
            bounced = true;
        }

        if (position.y < margin) {
            vy = Math.abs(vy);
            position.y = margin;
            bounced = true;
        } else if (position.y > height - margin) {
            vy = -Math.abs(vy);
            position.y = height - margin;
            bounced = true;
        }

        // Normalisera hastigheten efter studs
        if (bounced) {
            double currentSpeed = Math.sqrt(vx * vx + vy * vy);
            if (currentSpeed > 0) {
                vx = (vx / currentSpeed) * speed;
                vy = (vy / currentSpeed) * speed;
            }
        }
    }

    public void randomChange() {
        if (Math.random() < 0.02) {
            double angle = Math.atan2(vy, vx) + (Math.random() - 0.5) * Math.toRadians(60);
            vx = Math.cos(angle) * speed;
            vy = Math.sin(angle) * speed;
        }
    }
}