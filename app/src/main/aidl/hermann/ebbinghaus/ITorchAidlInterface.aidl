// ITorchAidlInterface.aidl
package hermann.ebbinghaus;

// Declare any non-default types here with import statements

interface ITorchAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);

	void setPlayListData(in List<String> musicList);

	void startPlay();

	int getMusicCurPosition();

	void setProcess(int time);

	boolean playOrPauseButton();

	void playNext();

	void playPrev();

}
