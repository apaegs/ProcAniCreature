package org.example;

import java.awt.geom.Point2D;

public class Target {
    public Point2D.Double position;
    public double vx, vy;
    public double speed;

    public Target(double startX, double startY, double vx, double vy, double speed) {
        position = new Point2D.Double(startX, startY);
        this.speed = speed;

        // Normalisera initial hastighet
        double currentSpeed = Math.sqrt(vx * vx + vy * vy);
        if (currentSpeed > 0) {
            this.vx = (vx / currentSpeed) * speed;
            this.vy = (vy / currentSpeed) * speed;
        } else {
            this.vx = speed;
            this.vy = 0;
        }
    }

    public void update(double deltaTime, int width, int height) {
        int margin = 20;

        // Uppdatera position
        position.x += vx * deltaTime;
        position.y += vy * deltaTime;

        // Studsa från kanterna
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
        // 2% chans per frame att ändra riktning
        if (Math.random() < 0.02) {
            double currentAngle = Math.atan2(vy, vx);
            // Ändra riktning med ±30 grader
            double angleChange = (Math.random() - 0.5) * Math.toRadians(60);
            double newAngle = currentAngle + angleChange;

            vx = Math.cos(newAngle) * speed;
            vy = Math.sin(newAngle) * speed;
        }
    }
}