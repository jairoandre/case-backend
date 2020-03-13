# case-backend project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

You can see the live demo of this project on this link: [https://stuffs.appspot.com](https://stuffs.appspot.com)

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw compile quarkus:dev
```

This project use the Google Cloud Datastore as database, so you need to have a Datastore (not Firebase) configured and the Account Service key (JSON file) for access it.

You need to pass the path of the credential JSON file before run the application:

```
export GOOGLE_CLOUD_CREDENTIALS_PATH=/path/to/the/credential.json
```

## Build the application

```
./mvnw compile quarkus:build
```

### Google App Engine

To deploy the app on the Google App Engine:

```
gcloud app deploy
```

You need to configure the "dispatch.yaml" file so the frontend project can access the backend:

```
gcloud app deploy dispatch.yaml
```

On Google App Engine there is no need to pass the credentials json file.


## TODO:

- Write unit tests
- Asynchronous data import


