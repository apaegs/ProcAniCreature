package org.example;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Fish {
    public Point2D.Double head;
    public List<FishSegment> segments = new ArrayList<>();
    private List<Integer> baseSizes;

    public double fishSpeed = 80;
    public double fishTurnSpeed = Math.toRadians(360);
    public double rigidity = 0.8;
    public double segmentDistance = 25;
    public double peakPosition = 0.3;
    public double sizeScale = 1.0;

    public Fish(int segmentCount, List<Integer> sizes, double startX, double startY) {
        this.baseSizes = new ArrayList<>(sizes);
        head = new Point2D.Double(startX, startY);
        for (int i = 0; i < segmentCount; i++) {
            int size = i < sizes.size() ? sizes.get(i) : 4;
            segments.add(new FishSegment(head.x - (i + 1) * segmentDistance, head.y, size));
        }
        updateSegmentSizes();
    }

    public void moveTowards(Point2D.Double target, double deltaTime) {
        double dx = target.x - head.x;
        double dy = target.y - head.y;
        double targetAngle = Math.atan2(dy, dx);

        double currentHeadAngle = getHeadAngle();

        double angleDiff = targetAngle - currentHeadAngle;
        while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
        while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

        double maxTurn = fishTurnSpeed * deltaTime;
        angleDiff = Math.max(-maxTurn, Math.min(maxTurn, angleDiff));
        double newHeadAngle = currentHeadAngle + angleDiff;

        head.x += Math.cos(newHeadAngle) * fishSpeed * deltaTime;
        head.y += Math.sin(newHeadAngle) * fishSpeed * deltaTime;

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
                double targetX = prev.x - dirX * segmentDistance;
                double targetY = prev.y - dirY * segmentDistance;
                seg.position.x += (targetX - seg.position.x) * rigidity;
                seg.position.y += (targetY - seg.position.y) * rigidity;

                seg.angle = Math.atan2(prev.y - seg.position.y, prev.x - seg.position.x);
            }
            prev = seg.position;
        }
    }

    public void updateSegments(int newCount, List<Integer> newBaseSizes) {
        this.baseSizes = new ArrayList<>(newBaseSizes);
        int currentCount = segments.size();

        if (newCount > currentCount) {
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
        this.peakPosition = newPeakPosition;
        updateSegmentSizes();
    }

    private void updateSegmentSizes() {
        if (segments.isEmpty() || baseSizes.isEmpty()) return;

        for (int i = 0; i < segments.size(); i++) {
            int baseSize = i < baseSizes.size() ? baseSizes.get(i) : 4;

            double progress = (double) i / (segments.size() - 1);
            double peakFactor;

            if (progress <= peakPosition) {
                peakFactor = progress / peakPosition;
            } else {
                peakFactor = 1.0 - ((progress - peakPosition) / (1.0 - peakPosition));
            }

            peakFactor = smoothStep(peakFactor);
            int size = (int)(baseSize * peakFactor * sizeScale);
            segments.get(i).size = Math.max(2, size);
        }
    }

    private double smoothStep(double x) {
        return x * x * (3 - 2 * x);
    }
}