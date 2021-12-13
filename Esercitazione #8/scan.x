typedef string String <255>;

struct params {
	String dir;
	int min;
};

struct response {
    int chars;
    int words;
    int rows;
};


program SCANPROG {
	version SCANVERS {
		response FILE_SCAN(String) = 1;
		int DIR_SCAN(params) = 2;
	} = 1;
} = 0x20000013;
