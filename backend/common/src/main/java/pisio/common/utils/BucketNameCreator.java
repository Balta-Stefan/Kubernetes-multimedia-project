package pisio.common.utils;

public class BucketNameCreator
{
    private static final String userBucketPrefix = "user-";

    public static String createBucket(int userID)
    {
        return userBucketPrefix + userID;
    }
}
