package com.company.Encoding;

public class Node implements Comparable {
    private String word;
    private long occurrence;
    private String code;

    private Node left;
    private Node right;

    public Node(String word, long occurrence) {
        this.word = word;
        this.occurrence = occurrence;
        this.left = null;
        this.right = null;
    }

    public Node(Node left, Node right) {
        this.occurrence = left.getOccurrence() + right.getOccurrence();
        this.left = left;
        this.right = right;
    }

    public String getWord() {
        return word;
    }

    public long getOccurrence() {
        return occurrence;
    }

    public String getCode() {
        return code;
    }

    @Override
    public int compareTo(Object node) {
        long occurrence = ((Node) node).getOccurrence();
        return (int) (this.occurrence - occurrence);
    }

    @Override
    public String toString() {
        return "Node{" +
                "word='" + word + '\'' +
                ", occurrence=" + occurrence +
                ", code='" + code + '\'' +
//                ", left=" + left +
//                ", right=" + right +
                '}';
    }

    public void setCode() {
        if (left != null) {
            left.setCode("0");
        }
        if (right != null) {
            right.setCode("1");
        }
        if (left == null && right == null) {
            this.code = "0";
        }
    }

    public void setCode(String code) {
        if (left != null) {
            left.setCode(code + "0");
        }
        if (right != null) {
            right.setCode(code + "1");
        }
        if (left == null && right == null) {
            this.code = code;

        }
    }

    public void print() {
        if (left == null && right == null) {
            System.out.println(toString());
        } else {
            if (left != null) {
                left.print();
            }
            if (right != null) {
                right.print();
            }
        }
    }

    public String getTreeAsString(String current) {
        if (left == null && right == null) {
            return current + "1" + word;
        }
        String leftSide = left.getTreeAsString(current + "0");
        String rightSide = right.getTreeAsString(leftSide);
        return rightSide;
    }
}
