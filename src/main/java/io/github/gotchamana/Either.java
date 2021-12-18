package io.github.gotchamana;

import java.util.function.Consumer;

public class Either<T, U> {

    private T left;
    private U right;

    private Either() {}

    public static <T, U> Either<T, U> left(T left) {
        var either = new Either<T, U>();
        either.left = left;
        return either;
    }

    public static <T, U> Either<T, U> right(U right) {
        var either = new Either<T, U>();
        either.right = right;
        return either;
    }

    public void run(Consumer<T> left, Consumer<U> right) {
        if (this.left != null)
            left.accept(this.left);
        else
            right.accept(this.right);
    }

    public T getLeft() {
        return left;
    }

    public U getRight() {
        return right;
    }
}