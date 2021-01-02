package com.company.Decoding;

public class Node {
    private Decoder decoder;
    private int level;

    private boolean isLeaf;
    private String word;
    private Node left;
    private Node right;

    public Node(Decoder decoder, int level) {
        this.decoder = decoder;
        this.level = level;
        this.isLeaf = false;
        left = null;
        right = null;
    }

    public Node(Decoder decoder, int level, String word) {
        this.decoder = decoder;
        this.level = level;
        this.isLeaf = true;
        this.word = word;
        left = null;
        right = null;
    }

    public boolean isTreeComplete() {
        if (isLeaf) {
            return true;//left == null && right == null;
        } else {
            if (left == null || right == null) {
                return false;
            }
            return left.isTreeComplete() && right.isTreeComplete();
        }
    }

    @Override
    public String toString() {
        return "Node{" +
                "decoder=" + decoder +
                ", level=" + level +
                ", isLeaf=" + isLeaf +
                ", word='" + word + '\'' +
                ", left=" + left +
                ", right=" + right +
                '}';
    }

    public boolean insert(boolean isLeaf, String word) {
        if (this.isLeaf) {
            return false;
        }
        if (left == null) {
            if (isLeaf) {
                left = new Node(decoder, level + 1, word);
            } else {
                left = new Node(decoder, level + 1);
            }
            return true;
        }
        boolean leftSuccess = left.insert(isLeaf, word);
        if (leftSuccess) {
            return true;
        }
        if (right == null) {
            if (isLeaf) {
                right = new Node(decoder, level + 1, word);
            } else {
                right = new Node(decoder, level + 1);
            }
            return true;
        }
        return right.insert(isLeaf, word);
    }

    public void print() {
        System.out.println(toString());
        if (left == null && right == null) {
//            System.out.println(toString());
            return;
        } else {

            if (left != null) {
                left.print();
            }
            if (right != null) {
                right.print();
            }
        }
    }

    public int getBiggestWordLength() {
        if (isLeaf) {
            return level;
        }
        int leftSide = left.getBiggestWordLength();
        int rightSide = right.getBiggestWordLength();
        return Math.max(leftSide, rightSide);
    }

    public String getWord(String codeBytes) {
        if (isLeaf) {
            decoder.updateUnresolved(level);
            return word;
        }
        if (codeBytes.substring(0, 1).equals("0")) {
            return left.getWord(codeBytes.substring(1));
        }
        return right.getWord(codeBytes.substring(1));
    }
}
