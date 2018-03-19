package main;

import java.time.LocalDateTime;

public interface Entry<T> {
	LocalDateTime getTime();
	T getID();
}