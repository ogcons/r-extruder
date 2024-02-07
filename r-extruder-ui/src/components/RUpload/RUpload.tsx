import { DropDownList } from "@progress/kendo-react-dropdowns";
import { Loader } from "@progress/kendo-react-indicators";
import React, { useState } from "react";
import {
  Upload,
  UploadFileInfo,
  UploadOnAddEvent,
  UploadOnRemoveEvent,
} from "@progress/kendo-react-upload";
import { Field, Form } from "@progress/kendo-react-form";
import { Checkbox } from "@progress/kendo-react-inputs";
import "./RUpload.scss";

interface FieldRenderPropsDropdown {
  value: string;
  onChange: (value: { value: string }) => void;
}
interface EventDropdown {
  target: {
    value: {
      id: string;
    };
  };
}

interface FieldRenderProps {
  value: UploadFileInfo[];
  onChange: (value: { value: UploadFileInfo[] }) => void;
}

let checkedName = false;

const DropDownListInput = (
  fieldRenderPropsDropdown: FieldRenderPropsDropdown
) => {
  const { value, onChange, ...others } = fieldRenderPropsDropdown;
  const data = [
    {
      id: "getId",
      name: "Get ID",
    },
    {
      id: "getDocument",
      name: "Get document",
    },
  ];
  const handleDropDownChange = (event: EventDropdown) => {
    const outputValue = event.target.value.id === "getDocument" ? "docx" : "id";
    onChange({ value: outputValue });
  };
  return (
    <DropDownList
      data={data}
      textField="name"
      dataItemKey="id"
      value={data.find((item) => item.id === value)}
      onChange={handleDropDownChange}
      {...others}
    />
  );
};

const UploadInput = (fieldRenderProps: FieldRenderProps) => {
  const handleFileChange = (event: UploadOnAddEvent | UploadOnRemoveEvent) => {
    checkedName = false;
    event.newState.forEach((file) => {
      if (!file.name.endsWith(".R")) {
        checkedName = true;
      }
    });
    fieldRenderProps.onChange({ value: event.newState });
  };

  return (
    <Upload
      autoUpload={false}
      showActionButtons={false}
      files={fieldRenderProps.value}
      onAdd={handleFileChange}
      onRemove={handleFileChange}
    />
  );
};

type FileWithRaw = {
  getRawFile: () => File;
};
const RUpload = () => {
  const [s3Key, setS3Key] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [conversionComplete, setConversionComplete] = useState<boolean>(false);
  const [timeoutId, setTimeoutId] = useState<NodeJS.Timeout | null>(null);

  const handleSubmit = async (dataItem: {
    // This is temporary solution, since this will not be used if project becomes official
    // eslint-disable-next-line
    [name: string]: any;
  }) => {
    setLoading(true);
    setConversionComplete(false);
    const { files, ...otherFields } = dataItem;

    const formData = new FormData();

    files.forEach((file: FileWithRaw) => {
      formData.append("files", file.getRawFile());
    });

    Object.keys(otherFields).forEach((prop) => {
      formData.append(prop, otherFields[prop]);
    });

    try {
      const response = await fetch("http://localhost:8080/api/extractors", {
        method: "POST",
        body: formData,
      });

      if (dataItem.output === "docx") {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const word = document.createElement("a");
        word.href = url;
        word.download = files[0].getRawFile().name.replace(".R", ".docx");
        document.body.appendChild(word);
        word.click();
        word.remove();
        setConversionComplete(true);

        if (timeoutId) clearTimeout(timeoutId);

        const id = setTimeout(() => {
          setConversionComplete(false);
        }, 5000);
        setTimeoutId(id);
      } else {
        const data = await response.json();
        setS3Key(data["s3 key"]);
        setConversionComplete(true);

        if (timeoutId) clearTimeout(timeoutId);

        const id = setTimeout(() => {
          setConversionComplete(false);
        }, 5000);
        setTimeoutId(id);
      }
    } catch (error) {
      console.error("Error:", error);
    } finally {
      setLoading(false);
    }
  };
  const copyToClipboard = () => {
    if (s3Key) {
      navigator.clipboard
        .writeText(s3Key)
        .then(() => {})
        .catch((error) => {
          console.error("Error copying S3 key to clipboard:", error);
        });
    }
  };
  return (
    <div className="loader">
      {loading && (
        <div className="loader-overlay">
          <Loader type="converging-spinner" />
        </div>
      )}

      {conversionComplete && (
        <div className="alert">
          <div className="success-alert">
            Your conversion is complete!{" "}
            <button onClick={() => setConversionComplete(false)}>
              <svg
                fill="#ffffff"
                height="20px"
                width="20px"
                version="1.1"
                id="Capa_1"
                xmlns="http://www.w3.org/2000/svg"
                xmlnsXlink="http://www.w3.org/1999/xlink"
                viewBox="0 0 460.775 460.775"
                xmlSpace="preserve"
                stroke="#ffffff"
              >
                <g id="SVGRepo_bgCarrier" strokeWidth="0"></g>
                <g
                  id="SVGRepo_tracerCarrier"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                ></g>
                <g id="SVGRepo_iconCarrier">
                  <path d="M285.08,230.397L456.218,59.27c6.076-6.077,6.076-15.911,0-21.986L423.511,4.565c-2.913-2.911-6.866-4.55-10.992-4.55 c-4.127,0-8.08,1.639-10.993,4.55l-171.138,171.14L59.25,4.565c-2.913-2.911-6.866-4.55-10.993-4.55 c-4.126,0-8.08,1.639-10.992,4.55L4.558,37.284c-6.077,6.075-6.077,15.909,0,21.986l171.138,171.128L4.575,401.505 c-6.074,6.077-6.074,15.911,0,21.986l32.709,32.719c2.911,2.911,6.865,4.55,10.992,4.55c4.127,0,8.08-1.639,10.994-4.55 l171.117-171.12l171.118,171.12c2.913,2.911,6.866,4.55,10.993,4.55c4.128,0,8.081-1.639,10.992-4.55l32.709-32.719 c6.074-6.075,6.074-15.909,0-21.986L285.08,230.397z"></path>
                </g>
              </svg>
            </button>
          </div>
        </div>
      )}
      <Form
        onSubmit={handleSubmit}
        render={(formRenderProps) => (
          <form onSubmit={formRenderProps.onSubmit} className={"k-form"}>
            <fieldset>
              <legend>Upload your R files:</legend>
              <div className="upload-input">
                <Field name={"files"} component={UploadInput} />
              </div>
              <div className="dropdown-list">
                <Field
                  name={"output"}
                  component={DropDownListInput}
                  label={"Select your returning parameter"}
                />
              </div>
              <div className="checkbox">
                <Field
                  name={"pdf"}
                  component={Checkbox}
                  label={
                    "Do you want whole plot movement instead of one picture?"
                  }
                />
              </div>
              <div className="s3">
                Your S3 key for download:{" "}
                {s3Key && (
                  <>
                    <input type="text" value={` ${s3Key}`} readOnly />
                    <button type="button" onClick={copyToClipboard}>
                      <svg
                        fill="#ffffff"
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 52 52"
                        enableBackground="new 0 0 52 52"
                        xmlSpace="preserve"
                        stroke="#ffffff"
                      >
                        <g id="SVGRepo_bgCarrier" strokeWidth="0"></g>
                        <g
                          id="SVGRepo_tracerCarrier"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                        ></g>
                        <g id="SVGRepo_iconCarrier">
                          <g>
                            <path d="M17.4,11.6h17.3c0.9,0,1.6-0.7,1.6-1.6V6.8c0-2.6-2.1-4.8-4.7-4.8h-11c-2.6,0-4.7,2.2-4.7,4.8V10 C15.8,10.9,16.5,11.6,17.4,11.6z"></path>
                            <path d="M43.3,6h-1.6c-0.5,0-0.8,0.3-0.8,0.8V10c0,3.5-2.8,6.4-6.3,6.4H17.4c-3.5,0-6.3-2.9-6.3-6.4V6.8 c0-0.5-0.3-0.8-0.8-0.8H8.7C6.1,6,4,8.2,4,10.8v34.4C4,47.8,6.1,50,8.7,50h34.6c2.6,0,4.7-2.2,4.7-4.8V10.8C48,8.2,45.9,6,43.3,6z"></path>
                          </g>
                        </g>
                      </svg>
                    </button>
                  </>
                )}
              </div>
              <div className="submit-button">
                {checkedName && <p> Only .R files are allowed.</p>}
                <button
                  type={"submit"}
                  className="k-button"
                  disabled={!formRenderProps.allowSubmit}
                >
                  Submit
                </button>
              </div>
            </fieldset>
          </form>
        )}
      />
    </div>
  );
};

export default RUpload;
