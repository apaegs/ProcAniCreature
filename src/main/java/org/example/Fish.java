package org.example;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Fish {
    public Point2D.Double head;
    public List<FishSegment> segments = new ArrayList<>();

    public double fishSpeed = 80;
    public double fishTurnSpeed = Math.toRadians(360);
    public double rigidity = 0.8;
    public double segmentDistance = 25; // NY: Avstånd mellan segment

    public Fish(int segmentCount, List<Integer> sizes, double startX, double startY) {
        head = new Point2D.Double(startX, startY);
        for (int i = 0; i < segmentCount; i++) {
            int size = i < sizes.size() ? sizes.get(i) : 4;
            segments.add(new FishSegment(head.x - (i + 1) * segmentDistance, head.y, size));
        }
    }

    public void moveTowards(Point2D.Double target, double deltaTime) {
        // --- Räkna ut vinkel mot target ---
        double dx = target.x - head.x;
        double dy = target.y - head.y;
        double targetAngle = Math.atan2(dy, dx);

        // --- Beräkna nuvarande huvudriktning ---
        double currentHeadAngle = getHeadAngle();

        // --- Jämna ut vinkelskillnaden ---
        double angleDiff = targetAngle - currentHeadAngle;
        while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
        while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

        // --- Begränsa svängningen ---
        double maxTurn = fishTurnSpeed * deltaTime;
        angleDiff = Math.max(-maxTurn, Math.min(maxTurn, angleDiff));
        double newHeadAngle = currentHeadAngle + angleDiff;

        // --- Uppdatera huvudposition ---
        head.x += Math.cos(newHeadAngle) * fishSpeed * deltaTime;
        head.y += Math.sin(newHeadAngle) * fishSpeed * deltaTime;

        // --- Segment följer (enklare metod från originalkoden) ---
        updateSegmentsPosition();
    }

    public double getHeadAngle() {
        if (segments.isEmpty()) {
            return 0;
        }
        FishSegment firstSegment = segments.get(0);
        return Math.atan2(head.y - firstSegment.position.y, head.x - firstSegment.position.x);
    }

    private void updateSegmentsPosition() {
        Point2D.Double prev = head;
        for (FishSegment seg : segments) {
            double dxSeg = prev.x - seg.position.x;
            double dySeg = prev.y - seg.position.y;
            double dist = Math.sqrt(dxSeg * dxSeg + dySeg * dySeg);

            if (dist > 0.1) {
                double dirX = dxSeg / dist;
                double dirY = dySeg / dist;
                double targetX = prev.x - dirX * segmentDistance; // Använd segmentDistance
                double targetY = prev.y - dirY * segmentDistance; // Använd segmentDistance
                seg.position.x += (targetX - seg.position.x) * rigidity;
                seg.position.y += (targetY - seg.position.y) * rigidity;

                // Uppdatera segmentets vinkel
                seg.angle = Math.atan2(prev.y - seg.position.y, prev.x - seg.position.x);
            }
            prev = seg.position;
        }
    }

    public void updateSegments(int newCount, List<Integer> baseSizes) {
        int currentCount = segments.size();

        if (newCount > currentCount) {
            // Lägg till nya segment
            for (int i = currentCount; i < newCount; i++) {
                Point2D.Double lastPos;
                if (segments.isEmpty()) {
                    lastPos = head;
                } else {
                    lastPos = segments.get(segments.size() - 1).position;
                }

                int size = i < baseSizes.size() ? baseSizes.get(i) : 4;
                segments.add(new FishSegment(lastPos.x - segmentDistance, lastPos.y, size));
            }
        } else if (newCount < currentCount) {
            // Ta bort segment
            segments.subList(newCount, currentCount).clear();
        }
    }

    public void scaleSegments(double scale, List<Integer> baseSizes) {
        for (int i = 0; i < segments.size(); i++) {
            int base = i < baseSizes.size() ? baseSizes.get(i) : 4;
            segments.get(i).size = (int)(base * scale);
        }
    }

    // Uppdatera segmentavstånd
    public void updateSegmentDistance(double newDistance) {
        this.segmentDistance = newDistance;
        // Positionerna kommer att justeras automatiskt i nästa updateSegmentsPosition-anrop
    }
}