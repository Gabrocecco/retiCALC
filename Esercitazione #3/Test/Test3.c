#include <stdio.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <string.h>
#include <netdb.h>
#include <unistd.h>
#include <fcntl.h>
#include <signal.h>
#include <time.h>

int main(int argc, char** argv) {
    
    int maxLen = 0;
    int cur = 0;
    char curChar;
    int curRead;
    int curPos;
    
    struct timeval t1;
    struct timeval t2;
    gettimeofday(&t1,NULL);
    
    int fd = open(argv[1], O_RDONLY);
    if (fd < 0) {
        printf("Errore\n");
        exit(0);
    }
            
    while (read(fd, &curChar, sizeof(char)) > 0) {
       if (curChar == '\n' || curChar == ' ') {
            lseek(fd, maxLen, SEEK_CUR);
        } else {
            cur = 0;
            lseek(fd, -maxLen - 1, SEEK_CUR);
            do {
                curRead = read(fd, &curChar, sizeof(char));
                cur++;
                printf("%c\n", curChar);
            } while (curChar != '\n' && curChar != ' ' && curRead > 0);
            cur--;
            if (cur > maxLen) {
                maxLen = cur;
            }
        }
    }
    
    gettimeofday(&t2, NULL);
    
    printf("%d\n", maxLen);
    
    printf("%d\n" ,1000000 * t2.tv_sec + t2.tv_usec - 1000000 * t1.tv_sec - t1.tv_usec);
    
    return 0;
}
