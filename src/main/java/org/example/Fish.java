package org.example;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Fish {
    public List<FishSegment> segments = new ArrayList<>();
    private List<Integer> baseSizes;

    public double fishSpeed = 80;
    public double fishTurnSpeed = Math.toRadians(360);
    public double rigidity = 0.8;
    public double flexibility = 0.2;
    public double selfAvoidance = 0.5;
    public double maxAngleDiff = Math.toRadians(45);  // Max vinkelskillnad mellan segment
    public double segmentDistance = 25;
    public double peakPosition = 0.5;
    public double sizeScale = 1.0;
    public double bellyScale = 1.0;
    public double taperStrength = 1.0;

    public Fish(int segmentCount, List<Integer> sizes, double startX, double startY) {
        this.baseSizes = new ArrayList<>(sizes);

        // Skapa segment från startposition
        for (int i = 0; i < segmentCount; i++) {
            int size = i < sizes.size() ? sizes.get(i) : 20;
            segments.add(new FishSegment(startX - (i + 1) * segmentDistance, startY, size));
        }
        updateSegmentSizes();
    }

    public void moveTowards(Point2D.Double target, double deltaTime) {
        if (segments.isEmpty()) return;

        FishSegment firstSeg = segments.get(0);
        double dx = target.x - firstSeg.position.x;
        double dy = target.y - firstSeg.position.y;
        double targetAngle = Math.atan2(dy, dx);

        double currentHeadAngle = firstSeg.angle;

        // Beräkna kortaste vinkelskillnad
        double angleDiff = targetAngle - currentHeadAngle;
        while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
        while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

        // Begränsa svänghastighet
        double maxTurn = fishTurnSpeed * deltaTime;
        angleDiff = Math.max(-maxTurn, Math.min(maxTurn, angleDiff));
        double newHeadAngle = currentHeadAngle + angleDiff;

        // Uppdatera första segmentets position
        firstSeg.position.x += Math.cos(newHeadAngle) * fishSpeed * deltaTime;
        firstSeg.position.y += Math.sin(newHeadAngle) * fishSpeed * deltaTime;
        firstSeg.angle = newHeadAngle;

        updateSegmentsPosition();
    }

    public double getHeadAngle() {
        if (segments.isEmpty()) {
            return 0;
        }
        return segments.get(0).angle;
    }

    private void updateSegmentsPosition() {
        if (segments.isEmpty()) return;

        for (int i = 1; i < segments.size(); i++) {
            FishSegment currentSeg = segments.get(i);
            FishSegment prevSeg = segments.get(i - 1);

            double dxSeg = prevSeg.position.x - currentSeg.position.x;
            double dySeg = prevSeg.position.y - currentSeg.position.y;
            double dist = Math.sqrt(dxSeg * dxSeg + dySeg * dySeg);

            if (dist > 0.1) {
                double dirX = dxSeg / dist;
                double dirY = dySeg / dist;
                double targetX = prevSeg.position.x - dirX * segmentDistance;
                double targetY = prevSeg.position.y - dirY * segmentDistance;

                // Själv-undvikande: Kolla om vi är för nära andra segment
                if (selfAvoidance > 0.01) {
                    double avoidX = 0;
                    double avoidY = 0;
                    int avoidCount = 0;

                    for (int j = 0; j < segments.size(); j++) {
                        if (Math.abs(j - i) <= 2) continue; // Skippa närliggande segment

                        FishSegment other = segments.get(j);
                        double dx = currentSeg.position.x - other.position.x;
                        double dy = currentSeg.position.y - other.position.y;
                        double distance = Math.sqrt(dx * dx + dy * dy);

                        double minDistance = (currentSeg.size + other.size) * 0.5;
                        if (distance < minDistance && distance > 0.1) {
                            avoidX += (dx / distance) * (minDistance - distance);
                            avoidY += (dy / distance) * (minDistance - distance);
                            avoidCount++;
                        }
                    }

                    if (avoidCount > 0) {
                        targetX += avoidX * selfAvoidance;
                        targetY += avoidY * selfAvoidance;
                    }
                }

                // Interpolera position baserat på styvhet
                currentSeg.position.x += (targetX - currentSeg.position.x) * rigidity;
                currentSeg.position.y += (targetY - currentSeg.position.y) * rigidity;

                // Beräkna ny vinkel med böjlighet
                double naturalAngle = Math.atan2(prevSeg.position.y - currentSeg.position.y,
                        prevSeg.position.x - currentSeg.position.x);

                // Lägg till böjlighetseffekt baserat på position i kroppen
                double flexFactor = flexibility * (1.0 - (double)i / segments.size());
                double angleDiff = naturalAngle - prevSeg.angle;
                while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
                while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

                currentSeg.angle = prevSeg.angle + angleDiff * (1.0 + flexFactor);
            }
        }
    }

    public void updateSegments(int newCount, List<Integer> newBaseSizes) {
        this.baseSizes = new ArrayList<>(newBaseSizes);
        int currentCount = segments.size();

        if (newCount > currentCount) {
            // Lägg till nya segment
            for (int i = currentCount; i < newCount; i++) {
                Point2D.Double lastPos;
                double lastAngle;

                if (segments.isEmpty()) {
                    lastPos = new Point2D.Double(0, 0);
                    lastAngle = 0;
                } else {
                    FishSegment last = segments.get(segments.size() - 1);
                    lastPos = last.position;
                    lastAngle = last.angle;
                }

                int size = i < baseSizes.size() ? baseSizes.get(i) : 20;
                double newX = lastPos.x - Math.cos(lastAngle) * segmentDistance;
                double newY = lastPos.y - Math.sin(lastAngle) * segmentDistance;
                segments.add(new FishSegment(newX, newY, size));
            }
        } else if (newCount < currentCount) {
            // Ta bort segment från slutet
            segments.subList(newCount, currentCount).clear();
        }
        updateSegmentSizes();
    }

    public void scaleSegments(double scale, List<Integer> newBaseSizes) {
        this.baseSizes = new ArrayList<>(newBaseSizes);
        this.sizeScale = scale;
        updateSegmentSizes();
    }

    public void updateSegmentDistance(double newDistance) {
        this.segmentDistance = newDistance;
    }

    public void updatePeakPosition(double newPeakPosition) {
        this.peakPosition = Math.max(0.0, Math.min(1.0, newPeakPosition));
        updateSegmentSizes();
    }

    public void updateSegmentSizes() {
        if (segments.isEmpty() || baseSizes.isEmpty()) return;

        for (int i = 0; i < segments.size(); i++) {
            int baseSize = i < baseSizes.size() ? baseSizes.get(i) : 20;

            // Beräkna position längs varelsen (0 = fram, 1 = bak)
            double progress = segments.size() > 1 ? (double) i / (segments.size() - 1) : 0;

            // Beräkna storleksfaktor baserat på peakPosition
            double peakFactor;
            if (progress <= peakPosition) {
                // Från början till toppen
                peakFactor = peakPosition > 0 ? progress / peakPosition : 1.0;
            } else {
                // Från toppen till slutet
                double tailProgress = (progress - peakPosition) / (1.0 - peakPosition);
                peakFactor = 1.0 - Math.pow(tailProgress, taperStrength);
            }

            // Använd smooth interpolation
            peakFactor = smoothStep(peakFactor);

            // Lägg till bukmåga (mest i mitten)
            double bellyFactor = 1.0 - Math.abs(progress - 0.5) * 2.0;
            bellyFactor = bellyFactor * bellyFactor;
            double bellyBonus = (bellyScale - 1.0) * bellyFactor;

            // Beräkna slutlig storlek
            int size = (int)(baseSize * peakFactor * (1.0 + bellyBonus) * sizeScale);
            segments.get(i).size = Math.max(2, size);
        }
    }

    private double smoothStep(double x) {
        // Hermite interpolation för mjukare övergångar
        x = Math.max(0.0, Math.min(1.0, x));
        return x * x * (3 - 2 * x);
    }
}