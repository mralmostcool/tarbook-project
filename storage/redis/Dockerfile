FROM redis:7-alpine

# Create configuration directory and copy custom config
RUN mkdir -p /usr/local/etc/redis /data && chown -R redis:redis /data
COPY redis.conf /usr/local/etc/redis/redis.conf

VOLUME /data
WORKDIR /data

EXPOSE 6379

CMD ["redis-server", "/usr/local/etc/redis/redis.conf"]
