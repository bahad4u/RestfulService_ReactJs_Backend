package loader;

import exceptions.LoaderException;

public interface Loader {

	public abstract void load() throws LoaderException;
}
