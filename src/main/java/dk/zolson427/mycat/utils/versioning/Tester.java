package dk.zolson427.mycat.utils.versioning;

// From DogOnFire's Versioning in Werewolf
// https://github.com/DogOnFire/Werewolf
public interface Tester<T> {
    boolean isEnabled(T t);
}